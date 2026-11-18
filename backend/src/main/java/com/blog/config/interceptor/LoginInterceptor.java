package com.blog.config.interceptor;

import com.blog.common.constant.RedisKeyConstant;
import com.blog.common.context.UserContext;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ResultCode;
import com.blog.common.utils.JwtUtil;
import com.blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器：除了登录、注册、接口文档，其余接口都必须携带有效 token。
 *
 * <p>一次请求的执行顺序：
 * <pre>
 * 请求 → Filter → DispatcherServlet → 【preHandle】 → AOP 切面 → Controller → 【afterCompletion】 → 响应
 * </pre>
 *
 * <p>校验步骤：
 * <ol>
 *   <li>从请求头取 token</li>
 *   <li>JWT 验签 + 校验是否过期</li>
 *   <li>和 Redis 里存的 token 比对（处理 "退出登录" 和 "在别处重新登录" 的情况）</li>
 *   <li>通过后把用户信息放进 {@link UserContext}，后续 Service 直接取</li>
 * </ol>
 * 任何一步失败都抛 BusinessException(401)，由全局异常处理器返回 {"code":401,...}，前端收到后跳转登录页。
 *
 * <p>拦截哪些路径、放行哪些路径，在 {@link com.blog.config.WebMvcConfig} 里配置。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Controller 方法执行之前调用。返回 true 放行，返回 false 或抛异常则中断请求。
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 0. 不是 Controller 方法的请求直接放行：
        //    - 浏览器跨域时先发的 OPTIONS 预检请求（它不会带 token，拦了的话真正的请求就发不出去）
        //    - 静态资源请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头取出 token
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 2. 解析 token：验签失败、格式错误、已过期都会抛异常
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("无效的 token：{}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "token 无效，请重新登录");
        }
        Long userId = jwtUtil.getUserId(claims);

        // 3. 和 Redis 中的 token 比对
        //    - Redis 里没有：已退出登录（key 被删）或已过期
        //    - Redis 里有但不一样：该账号在别处重新登录了，旧 token 作废
        Object cachedToken = redisTemplate.opsForValue().get(RedisKeyConstant.LOGIN_TOKEN_KEY + userId);
        if (!token.equals(cachedToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }

        // 4. 校验通过，保存当前用户到 ThreadLocal
        UserContext.set(new UserContext.LoginUser(userId, jwtUtil.getUsername(claims)));
        return true;
    }

    /**
     * 整个请求结束后调用（无论成功还是异常都会执行，前提是 preHandle 返回了 true）。
     * 在这里清理 ThreadLocal，防止线程复用导致的数据串号和内存泄漏。
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    /**
     * 从请求头中提取 token。
     * 兼容两种格式：带前缀 "Bearer xxx"（前端 Axios 发送）和不带前缀 "xxx"（在 Knife4j 里手动粘贴）。
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(header)) {
            return null;
        }
        String prefix = jwtProperties.getTokenPrefix();
        if (header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return header.trim();
    }
}
