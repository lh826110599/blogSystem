package com.blog.common.context;

/**
 * 当前登录用户上下文，基于 ThreadLocal 实现。
 *
 * <p>为什么需要它？
 * 登录拦截器解析 token 后拿到了 userId，但 Service 层（比如发布文章要记录作者）也需要 userId。
 * 如果一路把 HttpServletRequest 传下去会很别扭。Tomcat 处理一次请求从头到尾用的是同一个线程，
 * 所以把用户信息放进 ThreadLocal，同一请求内任何地方都能取到：
 * <pre>
 * Long userId = UserContext.getUserId();
 * </pre>
 *
 * <p>⚠ 必须在请求结束时调用 {@link #clear()}。
 * Tomcat 线程是池化复用的，不清理的话下一个请求可能拿到上一个用户的信息，而且会造成内存泄漏。
 * 清理动作放在 LoginInterceptor.afterCompletion 里。
 */
public final class UserContext {

    /**
     * 当前登录用户的最小信息。用 record（Java 16+）定义不可变数据类，自动生成构造器、getter、equals。
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public record LoginUser(Long userId, String username) {
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 工具类不允许 new */
    private UserContext() {
    }

    /**
     * 保存当前登录用户（由登录拦截器调用）。
     */
    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /**
     * 获取当前登录用户，未登录时返回 null。
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户ID，未登录时返回 null。
     */
    public static Long getUserId() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.userId();
    }

    /**
     * 清除当前线程保存的用户信息（请求结束时必须调用）。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
