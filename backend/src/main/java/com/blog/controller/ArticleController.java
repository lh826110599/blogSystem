package com.blog.controller;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.entity.dto.ArticleDTO;
import com.blog.entity.dto.ArticleQueryDTO;
import com.blog.entity.vo.ArticleVO;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口。
 * <pre>
 * GET    /api/article/page?current=1&amp;size=10   分页列表（前端文章列表页）
 * GET    /api/article/{id}                     详情（前端编辑页回显）
 * POST   /api/article                          发布（前端文章新增页）
 * PUT    /api/article/{id}                     修改
 * DELETE /api/article/{id}                     删除
 * </pre>
 * GET /page 和 GET /{id} 不会冲突：Spring 优先匹配写死的路径，/page 不会被当成 id。
 */
@Tag(name = "文章管理")
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 分页查询。参数没有 @RequestBody：GET 请求的查询参数按名字绑定到 ArticleQueryDTO 的字段。
     * {@code @ParameterObject}（springdoc）：让接口文档把 DTO 的每个字段展示成单独的查询参数。
     */
    @Operation(summary = "分页查询文章")
    @GetMapping("/page")
    public Result<PageResult<ArticleVO>> page(@ParameterObject @Valid ArticleQueryDTO query) {
        return Result.success(articleService.page(query));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    @Operation(summary = "发布文章", description = "status=0 保存草稿，status=1 直接发布；返回文章ID")
    @PostMapping
    public Result<Long> publish(@RequestBody @Valid ArticleDTO dto) {
        return Result.success(articleService.publish(dto));
    }

    @Operation(summary = "修改文章", description = "只能修改自己的文章，标签按 tagIds 整体替换")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ArticleDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除文章", description = "只能删除自己的文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}
