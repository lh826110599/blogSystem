package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分类实体，对应 category 表。
 *
 * <p>分类字段简单、没有敏感信息，查询接口直接返回本实体给前端，不再单独写 VO
 * （所以加了 {@code @Schema} 文档注解和 {@code @JsonFormat} 时间格式）。
 */
@Data
@TableName("category")
@Schema(description = "文章分类")
public class Category {

    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序，越小越靠前")
    private Integer sort;

    /** {@code @JsonFormat}：LocalDateTime 返回给前端时格式化成 "2024-01-01 10:00:00"，否则默认是 "2024-01-01T10:00:00" */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 逻辑删除标记，前端不需要，不返回 */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
