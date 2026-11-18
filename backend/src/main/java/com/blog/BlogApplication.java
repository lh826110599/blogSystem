package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 博客系统后端启动类。
 *
 * <p>{@code @SpringBootApplication} 是三个注解的组合：
 * <ul>
 *   <li>{@code @SpringBootConfiguration}：标记这是一个配置类</li>
 *   <li>{@code @EnableAutoConfiguration}：根据 classpath 上的依赖自动配置 Bean（数据源、Redis、MVC 等）</li>
 *   <li>{@code @ComponentScan}：扫描当前包 com.blog 及其子包下的 @Component / @Service / @Controller 等</li>
 * </ul>
 * 所以启动类必须放在根包 com.blog 下，否则子包里的类扫描不到。
 *
 * <p>Mapper 接口的扫描（@MapperScan）放在 config.MybatisPlusConfig 里，让 MyBatis 相关配置集中在一处。
 */
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
