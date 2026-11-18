package com.blog.controller;

import com.blog.common.result.Result;
import com.blog.entity.Tag;
import com.blog.entity.dto.TagDTO;
import com.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
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
 * 标签接口，路径设计和 CategoryController 一致。
 * <pre>
 * GET    /api/tag/list   查询全部
 * POST   /api/tag        新增
 * PUT    /api/tag/{id}   修改
 * DELETE /api/tag/{id}   删除（同时解除和文章的关联）
 * </pre>
 *
 * <p>注意类名冲突：实体类叫 Tag，Swagger 的文档注解也叫 Tag。
 * 这里 import 了实体 com.blog.entity.Tag，所以文档注解要写全限定名 {@code @io.swagger.v3.oas.annotations.tags.Tag}。
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "查询全部标签")
    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.success(tagService.listAll());
    }

    @Operation(summary = "新增标签", description = "返回新标签ID")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid TagDTO dto) {
        return Result.success(tagService.create(dto));
    }

    @Operation(summary = "修改标签")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid TagDTO dto) {
        tagService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
