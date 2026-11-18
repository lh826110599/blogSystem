package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ResultCode;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.dto.TagDTO;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签业务实现。结构和 CategoryServiceImpl 基本一致，区别在删除逻辑。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<Tag> listAll() {
        return lambdaQuery().orderByAsc(Tag::getId).list();
    }

    @Override
    public Long create(TagDTO dto) {
        checkNameUnique(dto.getName(), null);

        Tag tag = new Tag();
        tag.setName(dto.getName());
        try {
            save(tag);
        } catch (DuplicateKeyException e) {
            // 并发创建同名标签，或同名标签曾被逻辑删除（仍占着唯一索引）
            throw new BusinessException(ResultCode.TAG_EXISTS);
        }
        return tag.getId();
    }

    @Override
    public void update(Long id, TagDTO dto) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }
        checkNameUnique(dto.getName(), id);

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(dto.getName());
        try {
            updateById(tag);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.TAG_EXISTS);
        }
    }

    /**
     * 删除标签。和分类不同，标签下有文章也允许删除：直接解除关联即可，文章本身不受影响。
     *
     * <p>{@code @Transactional}：两步操作要么都成功、要么都回滚，
     * 避免出现 "标签删了但关系还在" 或 "关系删了但标签还在" 的中间状态。
     * rollbackFor = Exception.class：默认只对 RuntimeException 回滚，加上后受检异常也回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }
        // 1. 逻辑删除标签
        removeById(id);
        // 2. 物理删除关系：DELETE FROM article_tag WHERE tag_id = ?
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getTagId, id));
    }

    /**
     * 校验标签名是否重复，修改时排除自己。
     */
    private void checkNameUnique(String name, Long excludeId) {
        Long count = lambdaQuery()
                .eq(Tag::getName, name)
                .ne(excludeId != null, Tag::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.TAG_EXISTS);
        }
    }
}
