package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.result.PageResult;
import com.blog.entity.Article;
import com.blog.entity.dto.ArticleDTO;
import com.blog.entity.dto.ArticleQueryDTO;
import com.blog.entity.vo.ArticleVO;

/**
 * 文章业务接口：发布、修改、删除、详情、分页列表。
 */
public interface ArticleService extends IService<Article> {

    /**
     * 发布文章（也可以保存为草稿，由 dto.status 决定）。作者为当前登录用户。
     *
     * @return 新文章ID
     */
    Long publish(ArticleDTO dto);

    /**
     * 修改文章。只有作者本人可以修改；标签按 dto.tagIds 整体替换。
     */
    void update(Long id, ArticleDTO dto);

    /**
     * 删除文章（逻辑删除）。只有作者本人可以删除。
     */
    void delete(Long id);

    /**
     * 文章详情（包含正文和标签），编辑页回显用。
     */
    ArticleVO getDetail(Long id);

    /**
     * 分页查询文章列表（不含正文）。
     */
    PageResult<ArticleVO> page(ArticleQueryDTO query);
}
