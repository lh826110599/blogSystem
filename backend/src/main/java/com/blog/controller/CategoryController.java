package com.blog.controller;

import com.blog.common.result.Result;
import com.blog.entity.Category;
import com.blog.entity.dto.CategoryDTO;
import com.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口，按 RESTful 风格设计：同一个资源路径，用不同的 HTTP 方法表示不同操作。
 * <pre>
 * GET    /api/category/list   查询全部
 * POST   /api/category        新增
 * PUT    /api/category/{id}   修改
 * DELETE /api/category/{id}   删除
 * </pre>
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "查询全部分类")
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "新增分类", description = "返回新分类ID")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    /**
     * {@code @PathVariable}：取 URL 路径里 {id} 的值。
     * pom.xml 开启了 -parameters 编译参数，参数名和占位符同名时不用写 @PathVariable("id")。
     */
    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类", description = "分类下还有文章时不能删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
