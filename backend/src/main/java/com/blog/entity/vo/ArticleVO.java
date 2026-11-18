package com.blog.entity.vo;

import com.blog.entity.Tag;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回给前端的文章数据，列表页和详情页共用。
 *
 * <p>字段来源：
 * <ul>
 *   <li>article 表本身的字段</li>
 *   <li>categoryName：LEFT JOIN category 得到</li>
 *   <li>authorName：LEFT JOIN user 得到（作者昵称）</li>
 *   <li>tags：多对多关系，不适合在同一条 SQL 里 JOIN（会让一篇文章变成多行），
 *       由 Service 再查一次 article_tag + tag 填进来</li>
 * </ul>
 * 列表查询不查 content（大字段，列表页用不到），所以列表接口里 content 为 null，详情接口才有值。
 *
 * <p>MyBatis 怎么把查询结果放进这个类？ArticleMapper.xml 里 resultType 指定本类，
 * 查询列 category_name 自动映射到属性 categoryName（驼峰映射），不需要手写 resultMap。
 */
@Data
@Schema(description = "文章信息")
public class ArticleVO {

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文（仅详情接口返回）")
    private String content;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "标签列表")
    private List<Tag> tags;

    @Schema(description = "状态：0草稿 1已发布")
    private Integer status;

    @Schema(description = "是否置顶：1是 0否")
    private Integer isTop;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
