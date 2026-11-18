package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.User;
import com.blog.entity.dto.LoginDTO;
import com.blog.entity.dto.RegisterDTO;
import com.blog.entity.vo.LoginVO;
import com.blog.entity.vo.UserVO;

/**
 * 用户业务接口：登录、注册、退出、获取当前用户。
 *
 * <p>为什么 Service 要分 "接口 + 实现类"？
 * Controller 只依赖接口，不关心具体实现；以后换实现（比如改成调用户中心服务）只改实现类。
 * 另外 Spring 的事务、AOP 是基于代理实现的，面向接口编程也是 Spring 的惯用写法。
 *
 * <p>继承 MyBatis-Plus 的 {@link IService}：自带 getById、save、updateById、removeById、
 * lambdaQuery() 等常用方法，比直接用 Mapper 多了批量操作和链式查询。
 */
public interface UserService extends IService<User> {

    /**
     * 登录：校验用户名密码，成功后生成 JWT 并存入 Redis。
     *
     * @param dto 用户名、密码
     * @return token 和用户信息
     */
    LoginVO login(LoginDTO dto);

    /**
     * 注册：检查用户名是否已存在，密码 BCrypt 加密后入库。
     *
     * @param dto 注册信息
     */
    void register(RegisterDTO dto);

    /**
     * 退出登录：删除 Redis 中当前用户的 token，旧 token 立即失效。
     */
    void logout();

    /**
     * 获取当前登录用户信息（前端刷新页面后用来恢复用户信息）。
     */
    UserVO getCurrentUser();
}
