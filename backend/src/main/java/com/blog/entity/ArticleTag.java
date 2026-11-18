package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章-标签关系实体，对应 article_tag 中间表。一行表示 "某篇文章挂了某个标签"。
 *
 * <p>关系表没有 deleted 字段：修改文章标签时直接物理删除旧关系、插入新关系，
 * 逻辑删除只会让中间表越积越多，还会和唯一索引 (article_id, tag_id) 冲突。
 */
@Data
@NoArgsConstructor
@TableName("article_tag")
public class ArticleTag {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long articleId;

    /** 标签ID */
    private Long tagId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 便捷构造器：new ArticleTag(articleId, tagId)。
     */
    public ArticleTag(Long articleId, Long tagId) {
        this.articleId = articleId;
        this.tagId = tagId;
    }
}
