package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Category;
import com.blog.entity.dto.CategoryDTO;

import java.util.List;

/**
 * 分类业务接口。分类数量很少，列表不分页，一次全部返回（前端下拉框直接用）。
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询全部分类，按 sort 升序。
     */
    List<Category> listAll();

    /**
     * 新增分类。
     *
     * @return 新分类的ID
     */
    Long create(CategoryDTO dto);

    /**
     * 修改分类。
     */
    void update(Long id, CategoryDTO dto);

    /**
     * 删除分类（逻辑删除）。分类下还有文章时不允许删除。
     */
    void delete(Long id);
}
