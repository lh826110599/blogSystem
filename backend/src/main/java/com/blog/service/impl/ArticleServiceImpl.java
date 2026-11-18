package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.context.UserContext;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.PageResult;
import com.blog.common.result.ResultCode;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.dto.ArticleDTO;
import com.blog.entity.dto.ArticleQueryDTO;
import com.blog.entity.vo.ArticleVO;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章业务实现，本项目里逻辑最多的一个类，建议重点看：
 * <ul>
 *   <li>{@link #publish} / {@link #update}：一次操作写两张表（article + article_tag），用事务保证一致</li>
 *   <li>{@link #page}：调用 XML 里的联表分页 SQL</li>
 *   <li>{@link #fillTags}：用 2 条 SQL 给一整页文章填充标签，避免 "N+1 查询"</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    /** 没填摘要时，从正文截取的字数 */
    private static final int AUTO_SUMMARY_LENGTH = 100;

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    /**
     * 发布文章。
     *
     * <p>{@code @Transactional}：先插 article 拿到 id，再插 article_tag。
     * 如果插标签时出错，前面插入的文章也会回滚，不会留下一篇 "没有标签的半成品"。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ArticleDTO dto) {
        List<Long> tagIds = normalizeTagIds(dto.getTagIds());
        checkCategoryExists(dto.getCategoryId());
        checkTagsExist(tagIds);

        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(resolveSummary(dto));
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus());
        article.setIsTop(dto.getIsTop() == null ? 0 : dto.getIsTop());
        // 作者 = 当前登录用户（LoginInterceptor 放进 UserContext 的）
        article.setAuthorId(UserContext.getUserId());
        // viewCount 等计数字段不 set，为 null 的字段不会出现在 INSERT 语句里，使用数据库默认值 0
        save(article);

        saveArticleTags(article.getId(), tagIds);
        log.info("发布文章：articleId={}, title={}", article.getId(), article.getTitle());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleDTO dto) {
        checkOwner(id);
        List<Long> tagIds = normalizeTagIds(dto.getTagIds());
        checkCategoryExists(dto.getCategoryId());
        checkTagsExist(tagIds);

        Article article = new Article();
        article.setId(id);
        article.setTitle(dto.getTitle());
        article.setSummary(resolveSummary(dto));
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus());
        // isTop 不传就是 null，updateById 会跳过它，保持原值
        article.setIsTop(dto.getIsTop());
        updateById(article);

        // 标签整体替换：先删掉这篇文章的全部旧关系，再插入新关系。
        // 比 "对比新旧列表算出增删" 简单得多，标签数量很少，性能没问题。
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getArticleId, id));
        saveArticleTags(id, tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        checkOwner(id);
        // 文章逻辑删除；关系表不做逻辑删除，直接物理删除
        removeById(id);
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getArticleId, id));
    }

    @Override
    public ArticleVO getDetail(Long id) {
        // baseMapper 是父类 ServiceImpl 注入好的 ArticleMapper
        ArticleVO vo = baseMapper.selectArticleDetail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        fillTags(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public PageResult<ArticleVO> page(ArticleQueryDTO query) {
        // Page 是 IPage 的实现类：传入页码和每页条数，分页插件会自动加上 COUNT 查询和分页语句
        Page<ArticleVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<ArticleVO> result = baseMapper.selectArticlePage(page, query);
        fillTags(result.getRecords());
        return PageResult.of(result);
    }

    // ============================== 私有方法 ==============================

    /**
     * 给一批文章填充标签。
     *
     * <p>反面做法（N+1 查询）：循环每篇文章，各查一次它的标签 → 一页 10 篇就是 1 + 10 次 SQL。
     * 本方法的做法：不管一页多少篇，固定只查 2 次：
     * <ol>
     *   <li>SELECT * FROM article_tag WHERE article_id IN (这页所有文章ID)</li>
     *   <li>SELECT * FROM tag WHERE id IN (上一步得到的所有标签ID)</li>
     * </ol>
     * 然后在内存里按 articleId 分组，放回每篇文章。
     */
    private void fillTags(List<ArticleVO> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return;
        }
        List<Long> articleIds = articles.stream().map(ArticleVO::getId).toList();

        // 1. 查这批文章的全部 文章-标签 关系
        List<ArticleTag> relations = articleTagMapper.selectList(
                Wrappers.<ArticleTag>lambdaQuery()
                        .in(ArticleTag::getArticleId, articleIds)
                        .orderByAsc(ArticleTag::getId));
        if (relations.isEmpty()) {
            articles.forEach(a -> a.setTags(new ArrayList<>()));
            return;
        }

        // 2. 一次查出涉及到的全部标签，转成 Map<标签ID, 标签>，方便按 ID 取
        //    selectBatchIds 会自动带上 deleted = 0，已删除的标签不会出现
        Set<Long> tagIds = relations.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        // 3. 按文章ID分组：Map<文章ID, 该文章的标签列表>
        Map<Long, List<Tag>> articleTagsMap = relations.stream()
                .filter(r -> tagMap.containsKey(r.getTagId()))
                .collect(Collectors.groupingBy(
                        ArticleTag::getArticleId,
                        Collectors.mapping(r -> tagMap.get(r.getTagId()), Collectors.toList())));

        // 4. 放回每篇文章，没有标签的给空列表（前端不用判断 null）
        articles.forEach(a -> a.setTags(articleTagsMap.getOrDefault(a.getId(), new ArrayList<>())));
    }

    /**
     * 校验文章存在，并且当前用户是作者。
     */
    private void checkOwner(Long articleId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        if (!Objects.equals(article.getAuthorId(), UserContext.getUserId())) {
            throw new BusinessException(ResultCode.ARTICLE_NO_PERMISSION);
        }
    }

    /**
     * 校验分类存在（前端传来的 categoryId 不可信，可能是随便填的）。
     */
    private void checkCategoryExists(Long categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 校验标签都存在：查出来的数量和传入的数量一致才算通过。
     */
    private void checkTagsExist(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        Long count = tagMapper.selectCount(Wrappers.<Tag>lambdaQuery().in(Tag::getId, tagIds));
        if (count != tagIds.size()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }
    }

    /**
     * 整理标签ID：null 转空列表，去掉 null 元素和重复ID（重复会违反唯一索引 uk_article_tag）。
     */
    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null) {
            return Collections.emptyList();
        }
        return tagIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * 保存文章-标签关系。一篇文章最多 10 个标签（ArticleDTO 里校验），逐条插入即可。
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            articleTagMapper.insert(new ArticleTag(articleId, tagId));
        }
    }

    /**
     * 摘要：填了就用填的；没填就去掉正文里常见的 Markdown 符号，截取前 100 个字。
     */
    private String resolveSummary(ArticleDTO dto) {
        if (StringUtils.hasText(dto.getSummary())) {
            return dto.getSummary().trim();
        }
        String plain = dto.getContent()
                .replaceAll("[#>*`~\\-\\[\\]()!|]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return plain.length() > AUTO_SUMMARY_LENGTH ? plain.substring(0, AUTO_SUMMARY_LENGTH) + "..." : plain;
    }
}
