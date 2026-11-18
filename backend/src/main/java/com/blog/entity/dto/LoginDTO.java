package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * 登录请求参数。
 *
 * <p>DTO（Data Transfer Object）：专门接收前端参数的对象。
 * 字段上的 jakarta.validation 注解在 Controller 方法参数加 {@code @Valid} 后生效，
 * 校验失败会抛 MethodArgumentNotValidException，由全局异常处理器返回 400 和 message 里的提示。
 *
 * <p>常用校验注解：
 * <ul>
 *   <li>{@code @NotNull}：不能为 null（适合数字、对象）</li>
 *   <li>{@code @NotBlank}：不能为 null、空串、纯空格（只用于字符串）</li>
 *   <li>{@code @NotEmpty}：不能为 null 或空（字符串、集合）</li>
 *   <li>{@code @Size(min, max)}：字符串长度、集合大小</li>
 *   <li>{@code @Min} / {@code @Max}：数字范围</li>
 *   <li>{@code @Pattern(regexp)}：正则匹配</li>
 * </ul>
 */
@Data
@Schema(description = "登录参数")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** {@code @ToString.Exclude}：AOP 日志切面打印参数时不会把明文密码打出来 */
    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    @Schema(description = "密码", example = "123456")
    private String password;
}
