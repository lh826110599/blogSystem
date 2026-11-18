package com.blog.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 文章分页查询条件，GET /article/page?current=1&size=10&keyword=xx&categoryId=1&status=1
 *
 * <p>GET 请求的查询参数会按名字自动绑定到这个对象的同名字段上（不需要 @RequestBody）。
 * 所有筛选条件都是选填，不传就不加对应的 WHERE 条件（见 ArticleMapper.xml 里的 &lt;if&gt;）。
 */
@Data
@Schema(description = "文章分页查询条件")
public class ArticleQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，从 1 开始", example = "1")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最多100")
    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "标题关键字，模糊匹配")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "状态：0草稿 1已发布，不传查全部")
    private Integer status;
}
