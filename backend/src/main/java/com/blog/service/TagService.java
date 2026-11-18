package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Tag;
import com.blog.entity.dto.TagDTO;

import java.util.List;

/**
 * 标签业务接口。和分类一样，列表不分页，一次全部返回。
 */
public interface TagService extends IService<Tag> {

    /**
     * 查询全部标签，按 ID 升序。
     */
    List<Tag> listAll();

    /**
     * 新增标签。
     *
     * @return 新标签的ID
     */
    Long create(TagDTO dto);

    /**
     * 修改标签名称。
     */
    void update(Long id, TagDTO dto);

    /**
     * 删除标签（逻辑删除），同时解除它和所有文章的关联。
     */
    void delete(Long id);
}
