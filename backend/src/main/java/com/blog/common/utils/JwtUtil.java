package com.blog.common.utils;

import com.blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成和解析 token。
 *
 * <p>JWT 由三段 Base64 组成，用 "." 连接：Header.Payload.Signature
 * <ul>
 *   <li>Header：签名算法，如 {"alg":"HS256"}</li>
 *   <li>Payload：存放的数据（claims），如用户ID、用户名、过期时间。
 *       <b>只是 Base64 编码，不是加密，任何人都能解码看到，所以绝不能放密码等敏感信息</b></li>
 *   <li>Signature：用密钥对前两段签名。服务端用同一密钥验签，内容被篡改就验不过</li>
 * </ul>
 *
 * <p>本项目的登录流程：
 * <ol>
 *   <li>登录成功 → {@link #generateToken} 生成 token，同时存一份到 Redis</li>
 *   <li>前端之后每次请求在请求头带上 Authorization: Bearer {token}</li>
 *   <li>LoginInterceptor 调用 {@link #parseToken} 验签、检查过期，再比对 Redis 里的 token</li>
 * </ol>
 *
 * <p>使用 jjwt 0.12.x 新版 API。
 */
@Component
public class JwtUtil {

    /** Payload 中存放用户名的 key（用户ID 放在标准字段 subject 里） */
    private static final String CLAIM_USERNAME = "username";

    private final JwtProperties jwtProperties;

    /** 签名密钥，启动时根据配置的 secret 生成一次，之后复用 */
    private final SecretKey secretKey;

    /**
     * 构造器注入配置并初始化密钥。
     * 如果 secret 不足 32 字节，Keys.hmacShaKeyFor 会直接抛 WeakKeyException，项目启动失败 —— 尽早暴露配置问题。
     */
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token。
     *
     * @param userId   用户ID，存入 subject（JWT 标准字段 "sub"）
     * @param username 用户名，存入自定义 claim
     * @return 形如 xxxxx.yyyyy.zzzzz 的 token 字符串（不带 Bearer 前缀）
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + jwtProperties.getExpire() * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))   // sub：主题，这里放用户ID
                .claim(CLAIM_USERNAME, username)   // 自定义数据
                .issuedAt(now)                     // iat：签发时间
                .expiration(expireAt)              // exp：过期时间，解析时 jjwt 会自动校验
                .signWith(secretKey)               // 签名；32 字节密钥会自动选用 HS256
                .compact();
    }

    /**
     * 解析并校验 token。
     *
     * @param token 不带 Bearer 前缀的 token
     * @return token 中的 Payload（Claims）
     * @throws JwtException 签名不对、格式错误、已过期（ExpiredJwtException）时抛出，由调用方处理
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)     // 用同一个密钥验签
                .build()
                .parseSignedClaims(token)  // 验签 + 校验过期时间，失败抛异常
                .getPayload();
    }

    /**
     * 从 Claims 中取出用户ID。
     */
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从 Claims 中取出用户名。
     */
    public String getUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * token 有效期（秒），登录时往 Redis 存 token 用同样的过期时间，保证两边同时失效。
     */
    public Long getExpireSeconds() {
        return jwtProperties.getExpire();
    }
}
