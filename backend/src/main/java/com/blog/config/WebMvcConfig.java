package com.blog.config;

import com.blog.config.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置：注册拦截器、配置跨域。
 *
 * <p>实现 {@link WebMvcConfigurer} 接口只覆盖需要的方法，其余保持 Spring Boot 默认配置。
 * （注意不要加 @EnableWebMvc，加了会让 Spring Boot 的 MVC 自动配置全部失效。）
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /**
     * 注册登录拦截器。
     *
     * <p>路径写法说明：这里的路径不包含 context-path（/api），
     * 例如 "/auth/login" 实际对应 http://localhost:8080/api/auth/login。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有接口
                .addPathPatterns("/**")
                // 放行不需要登录的接口
                .excludePathPatterns(
                        // 登录、注册
                        "/auth/login",
                        "/auth/register",
                        // Knife4j / springdoc 文档页面和数据
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/favicon.ico",
                        // Spring Boot 默认错误页
                        "/error"
                );
    }

    /**
     * 跨域配置。
     *
     * <p>前端开发时用 Vite 代理转发 /api 请求，浏览器认为是同源，其实不需要跨域。
     * 这里配置是为了：不走代理直接请求后端（比如用别的端口调试）时也能正常访问。
     * 只允许本机来源，上线时改成真实的前端域名。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                // 预检请求结果缓存 1 小时，期间同一接口不再重复发 OPTIONS
                .maxAge(3600);
    }
}
