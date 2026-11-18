package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体，对应 article 表。
 *
 * <p>返回给前端时不用本类，而是 {@link com.blog.entity.vo.ArticleVO}：
 * 前端还需要分类名、作者昵称、标签列表，这些不在 article 表里，要联表或二次查询拼出来。
 */
@Data
@TableName("article")
public class Article {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文（Markdown） */
    private String content;

    /** 封面图URL */
    private String cover;

    /** 分类ID → category.id */
    private Long categoryId;

    /** 作者ID → user.id，发布时取当前登录用户 */
    private Long authorId;

    /** 状态：0草稿 1已发布 */
    private Integer status;

    /**
     * 是否置顶：1是 0否。
     * 用 Integer 而不是 Boolean：Lombok 对 boolean isTop 会生成 isTop() / setTop()，
     * 属性名变成 top，和列名 is_top 对不上，是 MyBatis 里一个常见的坑。
     */
    private Integer isTop;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 评论数 */
    private Integer commentCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
