package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;

/**
 * 用户 Mapper（数据访问层）。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，不写一行 SQL 就拥有了单表 CRUD：
 * <ul>
 *   <li>insert(entity)：新增，自增主键会回填到 entity.id</li>
 *   <li>selectById(id) / selectList(wrapper) / selectOne(wrapper) / selectCount(wrapper)</li>
 *   <li>selectPage(page, wrapper)：分页查询（依赖 MybatisPlusConfig 里注册的分页插件）</li>
 *   <li>updateById(entity)：按主键更新，只更新非 null 字段</li>
 *   <li>deleteById(id)：按主键删除（配置了逻辑删除，实际是 UPDATE SET deleted = 1）</li>
 * </ul>
 * 不需要实现类：MybatisPlusConfig 上的 @MapperScan 会为接口生成代理对象并注册成 Bean。
 */
public interface UserMapper extends BaseMapper<User> {
}
