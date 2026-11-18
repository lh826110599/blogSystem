package com.blog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口文档配置。
 *
 * <p>Knife4j 是 springdoc（OpenAPI 3 规范）的增强 UI。启动后端后访问：
 * <b>http://localhost:8080/api/doc.html</b>
 *
 * <p>接口文档的内容来自 Controller 上的注解：
 * <ul>
 *   <li>{@code @Tag(name = "文章管理")}：标在 Controller 类上，作为接口分组</li>
 *   <li>{@code @Operation(summary = "发布文章")}：标在方法上，接口说明</li>
 *   <li>{@code @Schema(description = "标题")}：标在 DTO/VO 字段上，字段说明</li>
 * </ul>
 * 扫描范围在 application.yml 的 springdoc.group-configs 里配置。
 *
 * <p>调试需要登录的接口：先调 "登录" 接口拿到 token →
 * 点击页面左侧 "Authorize" → 填入 token → 之后所有请求都会自动带上 Authorization 请求头。
 */
@Configuration
public class Knife4jConfig {

    /** 安全方案名称，下面 Components 和 SecurityRequirement 要用同一个名字关联 */
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 文档基本信息
                .info(new Info()
                        .title("个人博客系统 API 文档")
                        .description("博客系统 V1：登录注册、分类、标签、文章")
                        .version("v1.0.0")
                        .contact(new Contact().name("blog")))
                // 声明一种鉴权方式：在请求头 Authorization 里携带 token
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(SECURITY_SCHEME_NAME)))
                // 全局启用上面的鉴权方式（登录、注册接口在后端拦截器里放行，带不带 token 都不影响）
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
