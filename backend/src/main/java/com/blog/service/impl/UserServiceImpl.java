package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.constant.RedisKeyConstant;
import com.blog.common.context.UserContext;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ResultCode;
import com.blog.common.utils.JwtUtil;
import com.blog.entity.User;
import com.blog.entity.dto.LoginDTO;
import com.blog.entity.dto.RegisterDTO;
import com.blog.entity.vo.LoginVO;
import com.blog.entity.vo.UserVO;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户业务实现。
 *
 * <p>继承 {@link ServiceImpl}&lt;UserMapper, User&gt;：
 * 父类已经注入了 UserMapper（通过 {@code baseMapper} 字段访问），并实现了 IService 的所有方法。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 账号状态：正常 */
    private static final int STATUS_NORMAL = 1;

    /**
     * BCrypt 密码加密器。
     * BCrypt 每次加密都会随机生成盐值，同一个密码两次加密结果不同，所以不能 "加密后比较字符串"，
     * 必须用 matches(明文, 密文) 校验。它是线程安全的，整个类共用一个实例即可。
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 按用户名查用户。lambdaQuery() 是 IService 提供的链式查询，User::getUsername 编译期就能检查字段名
        User user = lambdaQuery().eq(User::getUsername, dto.getUsername()).one();

        // 2. 用户不存在和密码错误返回同一个提示，避免被人用登录接口探测哪些用户名已经注册
        if (user == null || !PASSWORD_ENCODER.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 3. 账号被禁用
        if (user.getStatus() == null || user.getStatus() != STATUS_NORMAL) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 生成 token，并以相同的过期时间存入 Redis（重复登录会覆盖，旧 token 随之失效）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        redisTemplate.opsForValue().set(
                RedisKeyConstant.LOGIN_TOKEN_KEY + user.getId(),
                token,
                jwtUtil.getExpireSeconds(),
                TimeUnit.SECONDS);

        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        return new LoginVO(token, toUserVO(user));
    }

    @Override
    public void register(RegisterDTO dto) {
        // 1. 先查一次，给出友好提示
        Long count = lambdaQuery().eq(User::getUsername, dto.getUsername()).count();
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 组装实体：密码加密，昵称默认用用户名
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setStatus(STATUS_NORMAL);

        // 3. 入库。第 1 步的检查不能完全防重：两个人同时注册同一个用户名，可能都通过检查。
        //    真正兜底的是数据库唯一索引 uk_username，插入冲突时 Spring 抛 DuplicateKeyException，这里转成业务提示。
        //    （已逻辑删除的用户仍占着用户名，也会走到这里。）
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        log.info("新用户注册：userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        redisTemplate.delete(RedisKeyConstant.LOGIN_TOKEN_KEY + userId);
        log.info("用户退出登录：userId={}", userId);
    }

    @Override
    public UserVO getCurrentUser() {
        User user = getById(UserContext.getUserId());
        if (user == null) {
            // token 有效但用户已被删除
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return toUserVO(user);
    }

    /**
     * 实体转 VO。BeanUtils.copyProperties 按同名属性复制（UserVO 里没有 password，所以不会被复制过去）。
     */
    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
