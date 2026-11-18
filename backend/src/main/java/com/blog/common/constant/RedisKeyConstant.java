package com.blog.common.constant;

/**
 * Redis key 常量。
 *
 * <p>key 命名规范：项目名:模块:业务:标识，用冒号分隔。
 * 好处：Redis 可视化工具（如 RedisInsight）会按冒号自动分组显示，一眼就能看出 key 属于哪个模块；
 * 多个项目共用一个 Redis 时也不会冲突。
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    /**
     * 登录 token，完整 key = blog:login:token:{userId}，value = token 字符串，过期时间与 JWT 一致。
     *
     * <p>JWT 本身是无状态的，签发后在过期前一直有效，服务端没法主动让它失效。
     * 把 token 存进 Redis 后：
     * <ul>
     *   <li>退出登录：删除 key，旧 token 立刻失效</li>
     *   <li>重新登录：覆盖 key，旧设备上的 token 失效（单点登录效果）</li>
     * </ul>
     */
    public static final String LOGIN_TOKEN_KEY = "blog:login:token:";
}
