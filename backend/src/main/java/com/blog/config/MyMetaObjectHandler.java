package com.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 * <p>作用：执行 insert / update 时自动给 createTime、updateTime 赋值，业务代码里不用每次手动 set。
 *
 * <p>生效条件（两个都要满足）：
 * <ol>
 *   <li>这个类注册成 Bean（@Component）</li>
 *   <li>实体字段上标注了填充时机，例如：
 *     <pre>
 *     &#64;TableField(fill = FieldFill.INSERT)          // 只在插入时填充
 *     private LocalDateTime createTime;
 *
 *     &#64;TableField(fill = FieldFill.INSERT_UPDATE)   // 插入和更新时都填充
 *     private LocalDateTime updateTime;
 *     </pre>
 *   </li>
 * </ol>
 * 注意：只有通过 "实体对象" 执行的 insert / updateById 才会触发；
 * 用 UpdateWrapper 且不传实体的更新不会触发，那种情况靠数据库的 ON UPDATE CURRENT_TIMESTAMP 兜底。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充。
     * strictInsertFill：字段必须有 fill 注解、类型匹配、且当前值为 null 才会填充（不覆盖手动设置的值）。
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新时填充。
     *
     * <p>这里故意不用 strictUpdateFill：它只在字段为 null 时才填充。
     * 常见写法是 "先 selectById 查出实体 → 改几个字段 → updateById"，这时 updateTime 已有旧值，
     * strictUpdateFill 就不会覆盖，结果更新时间永远停在旧值。
     * setFieldValByName 是无条件覆盖，保证每次更新都刷新成当前时间。
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
    }
}
