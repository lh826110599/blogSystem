package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ResultCode;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.dto.CategoryDTO;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现。
 *
 * <p>这里注入的是 ArticleMapper 而不是 ArticleService：Service 之间互相注入容易形成循环依赖
 * （ArticleService 也要用分类），依赖底层 Mapper 更简单。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleMapper articleMapper;

    @Override
    public List<Category> listAll() {
        // 生成 SQL：SELECT ... FROM category WHERE deleted = 0 ORDER BY sort ASC, id ASC
        // （deleted = 0 是逻辑删除自动加的条件）
        return lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId)
                .list();
    }

    @Override
    public Long create(CategoryDTO dto) {
        checkNameUnique(dto.getName(), null);

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        try {
            save(category);
        } catch (DuplicateKeyException e) {
            // checkNameUnique 防不住并发；另外被逻辑删除的分类仍然占着唯一索引，重建同名分类也会走到这里
            throw new BusinessException(ResultCode.CATEGORY_EXISTS);
        }
        // insert 之后，自增主键会自动回填到实体的 id 字段
        return category.getId();
    }

    @Override
    public void update(Long id, CategoryDTO dto) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        checkNameUnique(dto.getName(), id);

        // 只 set 要修改的字段；updateById 默认忽略值为 null 的字段，不会把其他列清空
        Category category = new Category();
        category.setId(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort());
        try {
            updateById(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.CATEGORY_EXISTS);
        }
    }

    @Override
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        // 分类下还有文章（未删除的）就不允许删，否则这些文章会指向一个不存在的分类
        Long articleCount = articleMapper.selectCount(
                Wrappers.<Article>lambdaQuery().eq(Article::getCategoryId, id));
        if (articleCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_ARTICLE);
        }
        // 配置了逻辑删除，实际执行：UPDATE category SET deleted = 1 WHERE id = ? AND deleted = 0
        removeById(id);
    }

    /**
     * 校验分类名是否重复。
     *
     * @param name      分类名称
     * @param excludeId 修改时排除自己（不然改描述不改名字也会提示重复）；新增时传 null
     */
    private void checkNameUnique(String name, Long excludeId) {
        Long count = lambdaQuery()
                .eq(Category::getName, name)
                // 第一个参数是条件：excludeId != null 时才拼接 AND id <> ?
                .ne(excludeId != null, Category::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.CATEGORY_EXISTS);
        }
    }
}
