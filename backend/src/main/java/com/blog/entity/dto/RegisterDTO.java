package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 注册请求参数。
 *
 * <p>前端也会做一遍校验，但后端校验必不可少：别人可以绕过页面，直接用 Postman / curl 调接口。
 */
@Data
@Schema(description = "注册参数")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度为4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "用户名，4-20位字母、数字、下划线", example = "tom_2024")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @ToString.Exclude
    @Schema(description = "密码，6-20位", example = "123456")
    private String password;

    /** 选填，不填时后端默认使用用户名作为昵称 */
    @Size(max = 20, message = "昵称最多20个字符")
    @Schema(description = "昵称（选填）", example = "Tom")
    private String nickname;
}
