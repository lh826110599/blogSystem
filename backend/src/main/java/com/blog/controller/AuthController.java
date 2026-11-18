package com.blog.controller;

import com.blog.common.result.Result;
import com.blog.entity.dto.LoginDTO;
import com.blog.entity.dto.RegisterDTO;
import com.blog.entity.vo.LoginVO;
import com.blog.entity.vo.UserVO;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、注册、退出、当前用户。
 *
 * <p>Controller 层的职责只有三件事：接收参数 → 调用 Service → 包装成 Result 返回。
 * 不写业务逻辑，业务都在 Service 里。
 *
 * <p>常用注解：
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}，返回值自动转 JSON</li>
 *   <li>{@code @RequestMapping("/auth")}：类级别路径前缀，完整地址 = /api（context-path）+ /auth + 方法路径</li>
 *   <li>{@code @RequestBody}：把请求体 JSON 转成 Java 对象</li>
 *   <li>{@code @Valid}：触发 DTO 字段上的校验注解</li>
 *   <li>{@code @Tag} / {@code @Operation}：Knife4j 文档里的分组名和接口说明</li>
 * </ul>
 * {@code @RequiredArgsConstructor}（Lombok）为 final 字段生成构造器，Spring 通过构造器注入 UserService，
 * 这是官方推荐的注入方式（比字段上写 @Autowired 更容易测试，也能保证依赖不为 null）。
 */
@Tag(name = "认证管理", description = "登录、注册、退出")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 登录（不需要 token，在 WebMvcConfig 里放行）。
     * POST /api/auth/login  {"username":"admin","password":"123456"}
     */
    @Operation(summary = "登录", description = "成功后返回 token，之后的请求放到 Authorization 请求头")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    /**
     * 注册（不需要 token，在 WebMvcConfig 里放行）。
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 退出登录（需要 token）。
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 获取当前登录用户信息（需要 token）。
     */
    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(userService.getCurrentUser());
    }
}
