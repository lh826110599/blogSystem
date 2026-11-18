package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;

/**
 * 文章-标签关系 Mapper。
 *
 * <p>只用 BaseMapper 自带的方法就够了，Service 里会这样用：
 * <ul>
 *   <li>保存关系：循环 insert(new ArticleTag(articleId, tagId))</li>
 *   <li>删除关系：delete(条件 article_id = ?)</li>
 *   <li>批量查关系：selectList(条件 article_id IN (...))，用来给文章列表一次性填充标签</li>
 * </ul>
 */
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}
