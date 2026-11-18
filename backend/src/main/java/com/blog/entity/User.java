package com.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 user 表。
 *
 * <p>Entity / DTO / VO 的分工（本项目统一放在 entity 包下）：
 * <ul>
 *   <li>Entity（本类）：和数据库表一一对应，只在 mapper、service 内部流转</li>
 *   <li>DTO（entity.dto）：接收前端传来的参数，带校验注解，如 RegisterDTO</li>
 *   <li>VO（entity.vo）：返回给前端的数据，只包含前端需要的字段，如 UserVO（没有 password）</li>
 * </ul>
 *
 * <p>本类用到的 MyBatis-Plus 注解：
 * <ul>
 *   <li>{@code @TableName}：对应的表名。类名和表名一致时可以省略，写上更直观</li>
 *   <li>{@code @TableId(type = IdType.AUTO)}：主键，使用数据库自增</li>
 *   <li>{@code @TableField(fill = ...)}：自动填充，配合 config.MyMetaObjectHandler</li>
 *   <li>{@code @TableLogic}：逻辑删除字段，deleteById 实际执行 UPDATE SET deleted = 1</li>
 * </ul>
 * 字段名驼峰 createTime 自动对应列名 create_time（application.yml 开启了 map-underscore-to-camel-case）。
 */
@Data
@TableName("user")
public class User {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /**
     * BCrypt 加密后的密码。
     * {@code @ToString.Exclude}：打印日志时不输出；{@code @JsonIgnore}：万一被序列化成 JSON 也不输出。
     */
    @ToString.Exclude
    @JsonIgnore
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 账号状态：1正常 0禁用 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
