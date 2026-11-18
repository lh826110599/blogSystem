package com.blog.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功后返回的数据。
 *
 * <p>前端拿到后：
 * <ol>
 *   <li>token 存进 Zustand（持久化到 localStorage），之后每次请求放到 Authorization 请求头</li>
 *   <li>userInfo 用来在后台布局右上角显示昵称</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录结果")
public class LoginVO {

    @Schema(description = "JWT token（不带 Bearer 前缀）")
    private String token;

    @Schema(description = "当前用户信息")
    private UserVO userInfo;
}
