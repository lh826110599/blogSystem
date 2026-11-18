package com.blog.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 返回给前端的用户信息。
 *
 * <p>VO（View Object）：给前端看的数据。和 User 实体相比去掉了 password、status、deleted 等字段，
 * 从源头上保证敏感信息不会出现在接口响应里。
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;
}
