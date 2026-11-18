package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增 / 修改标签的请求参数。修改时标签ID 从路径 PUT /tag/{id} 里取。
 */
@Data
@Schema(description = "标签参数")
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最多50个字符")
    @Schema(description = "标签名称", example = "Spring Boot")
    private String name;
}
