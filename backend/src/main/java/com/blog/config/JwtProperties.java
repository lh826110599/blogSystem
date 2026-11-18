package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性，绑定 application.yml 里 jwt.* 开头的配置。
 *
 * <p>{@code @ConfigurationProperties} 相比逐个写 {@code @Value("${jwt.secret}")} 的好处：
 * 配置项集中在一个类里，有类型检查，yml 里的 token-prefix 会自动映射到 tokenPrefix（宽松绑定）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥，至少 32 个字符 */
    private String secret;

    /** token 有效期（秒） */
    private Long expire;

    /** 前端携带 token 的请求头名称 */
    private String header = "Authorization";

    /** token 前缀（注意末尾有空格） */
    private String tokenPrefix = "Bearer ";
}
