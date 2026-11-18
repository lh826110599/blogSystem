package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.blog.entity.Article;
import com.blog.entity.dto.ArticleQueryDTO;
import com.blog.entity.vo.ArticleVO;
import org.apache.ibatis.annotations.Param;

/**
 * 文章 Mapper。
 *
 * <p>单表增删改用 BaseMapper 自带方法；列表和详情需要联表（分类名、作者昵称），
 * MyBatis-Plus 的 Wrapper 只适合单表，所以这两个查询手写 SQL，放在
 * resources/mapper/ArticleMapper.xml（application.yml 的 mybatis-plus.mapper-locations 指定了 XML 位置）。
 *
 * <p>接口方法和 XML 的对应关系：XML 的 namespace = 本接口全限定名，&lt;select id&gt; = 方法名。
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询文章列表（联表查分类名、作者昵称，不查 content）。
     *
     * <p>第一个参数是 IPage 时，分页插件会自动：
     * <ol>
     *   <li>根据 XML 里的 SQL 生成 SELECT COUNT(*) 查总数</li>
     *   <li>在 SQL 末尾拼上分页语句</li>
     *   <li>把结果和总数一起放进返回的 IPage</li>
     * </ol>
     * 所以 XML 里不用写 LIMIT。
     *
     * @param page  分页参数（current、size）
     * @param query 查询条件；@Param("query") 让 XML 里可以用 #{query.keyword} 取值
     * @return 分页结果，records 里的 tags 为空，由 Service 填充
     */
    IPage<ArticleVO> selectArticlePage(IPage<ArticleVO> page, @Param("query") ArticleQueryDTO query);

    /**
     * 查询文章详情（包含 content）。
     *
     * @param id 文章ID
     * @return 文章详情，不存在或已删除时返回 null
     */
    ArticleVO selectArticleDetail(@Param("id") Long id);
}
