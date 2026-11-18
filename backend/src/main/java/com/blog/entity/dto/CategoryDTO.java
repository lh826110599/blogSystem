package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增 / 修改分类的请求参数。
 *
 * <p>新增和修改共用一个 DTO：修改时分类ID 从路径 PUT /category/{id} 里取，所以这里没有 id 字段。
 * 为什么不直接用 Category 实体接收？实体里有 createTime、deleted 等字段，
 * 前端如果恶意传了这些值，可能被直接写进数据库。DTO 只暴露允许前端修改的字段。
 */
@Data
@Schema(description = "分类参数")
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多50个字符")
    @Schema(description = "分类名称", example = "后端开发")
    private String name;

    @Size(max = 255, message = "分类描述最多255个字符")
    @Schema(description = "分类描述", example = "Java / Spring Boot 等后端技术")
    private String description;

    @Min(value = 0, message = "排序值不能小于0")
    @Schema(description = "排序，越小越靠前，不传默认 0", example = "1")
    private Integer sort;
}
