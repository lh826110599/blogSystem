package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布 / 修改文章的请求参数（前端 "文章新增页面" 提交的表单）。
 *
 * <p>没有 authorId 字段：作者就是当前登录用户，由后端从 UserContext 取。
 * 如果让前端传，别人就能冒充任意用户发文章。
 */
@Data
@Schema(description = "文章参数")
public class ArticleDTO {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题最多200个字符")
    @Schema(description = "标题", example = "Spring Boot 3 入门")
    private String title;

    @Size(max = 500, message = "文章摘要最多500个字符")
    @Schema(description = "摘要（选填，不填时后端截取正文前 100 字）")
    private String summary;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "正文（Markdown）", example = "# Hello\n这是正文")
    private String content;

    @Size(max = 255, message = "封面地址最多255个字符")
    @Schema(description = "封面图URL（选填）")
    private String cover;

    @NotNull(message = "请选择文章分类")
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /** 选填；修改文章时传入的是 "最新的完整标签列表"，后端会先删旧关系再插入 */
    @Size(max = 10, message = "一篇文章最多选择10个标签")
    @Schema(description = "标签ID列表（选填）", example = "[1, 2]")
    private List<Long> tagIds;

    @NotNull(message = "文章状态不能为空")
    @Min(value = 0, message = "文章状态只能是0或1")
    @Max(value = 1, message = "文章状态只能是0或1")
    @Schema(description = "状态：0草稿 1发布", example = "1")
    private Integer status;

    @Min(value = 0, message = "置顶标记只能是0或1")
    @Max(value = 1, message = "置顶标记只能是0或1")
    @Schema(description = "是否置顶：1是 0否，不传默认 0", example = "0")
    private Integer isTop;
}
