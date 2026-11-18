-- =====================================================================
-- 个人博客系统 V1 建表脚本
-- 数据库：MySQL 8.x        字符集：utf8mb4（完整支持 emoji）
--
-- 用途：mysql 环境（默认，见 application-mysql.yml）的建表脚本，在 MySQL 里手动执行一次。
--   执行方式：mysql -uroot -p --default-character-set=utf8mb4 -e "source backend/sql/blog_v1.sql"
--   h2 环境不用这个文件，用的是 src/main/resources/db/schema-h2.sql 和 data-h2.sql。
--
-- ⚠ 本脚本每张表前都有 DROP TABLE IF EXISTS，会清空同名表的数据，
--   只在「第一次初始化」或「想重置数据」时执行。
--
-- 设计约定（后端代码都基于这些约定）：
--   1. 主键统一 BIGINT 自增，对应 Java 的 Long。
--      不用雪花 ID 的原因：19 位数字超过 JS Number 安全范围，前端会丢精度。
--   2. create_time / update_time 由数据库默认值生成，
--      后端 MyBatis-Plus 的自动填充再兜底一次（双保险）。
--   3. deleted 是逻辑删除字段：0 未删除、1 已删除，对应 MyBatis-Plus 的 @TableLogic。
--      关系表（article_tag / user_role / role_menu / like / collect）直接物理删除，不加 deleted。
--   4. 不建物理外键，表之间的关联由业务代码维护
--      （互联网项目常见做法：避免级联锁表，方便以后分库分表）。
--
-- 表清单（9 张业务表 + 3 张关系表）：
--   user、role、menu、category、tag、article、comment、like、collect
--   user_role（用户-角色）、role_menu（角色-菜单）、article_tag（文章-标签）
--   关系表是多对多必须的中间表，不加的话一篇文章只能挂一个标签。
-- =====================================================================

-- 库名用全小写 blogsystem：Windows 版 MySQL 默认 lower_case_table_names=1，
-- 建库时写的 blogSystem 实际存成 blogsystem；Linux 上库名区分大小写，统一小写最省事。
CREATE DATABASE IF NOT EXISTS `blogsystem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `blogsystem`;

-- =====================================================================
-- 一、权限相关：user / role / menu / user_role / role_menu（经典 RBAC 模型）
--    V1 只用到 user 表做登录注册，role / menu 先建好，留给后续做权限控制
-- =====================================================================

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码：BCrypt 加密后的密文（60位），绝不存明文',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1正常 0禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码，如 ROLE_ADMIN（代码里做权限判断用）',
  `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称，如 管理员（页面展示用）',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ----------------------------
-- 3. 菜单表（树形结构：parent_id 指向父菜单）
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID，0 表示顶级菜单',
  `menu_name`   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
  `menu_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：0目录 1菜单 2按钮',
  `path`        VARCHAR(200) DEFAULT NULL COMMENT '前端路由地址，如 /admin/article/list',
  `component`   VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径，如 Article/ArticleList',
  `perms`       VARCHAR(100) DEFAULT NULL COMMENT '权限标识，如 article:add（按钮级权限用）',
  `icon`        VARCHAR(100) DEFAULT NULL COMMENT '菜单图标（Ant Design 图标名）',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `visible`     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否在侧边栏显示：1显示 0隐藏',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- ----------------------------
-- 4. 用户-角色关系表（一个用户可有多个角色）
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID → user.id',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID → role.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户-角色关系表';

-- ----------------------------
-- 5. 角色-菜单关系表（一个角色可访问多个菜单）
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID → role.id',
  `menu_id`     BIGINT   NOT NULL COMMENT '菜单ID → menu.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-菜单关系表';

-- =====================================================================
-- 二、内容相关：category / tag / article / article_tag
--    V1 实现这几张表的 CRUD
-- =====================================================================

-- ----------------------------
-- 6. 分类表（文章 : 分类 = 多对一）
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称（唯一）',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章分类表';

-- ----------------------------
-- 7. 标签表（文章 : 标签 = 多对多，通过 article_tag 关联）
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50) NOT NULL COMMENT '标签名称（唯一）',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章标签表';

-- ----------------------------
-- 8. 文章表
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`         VARCHAR(200) NOT NULL COMMENT '文章标题',
  `summary`       VARCHAR(500) DEFAULT NULL COMMENT '文章摘要（列表页展示，避免列表查询带出大字段 content）',
  `content`       LONGTEXT     NOT NULL COMMENT '文章正文（Markdown 原文）',
  `cover`         VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
  `category_id`   BIGINT       NOT NULL COMMENT '分类ID → category.id',
  `author_id`     BIGINT       NOT NULL COMMENT '作者ID → user.id',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0草稿 1已发布',
  `is_top`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：1是 0否',
  `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数（冗余计数，避免每次 COUNT like 表）',
  `collect_count` INT          NOT NULL DEFAULT 0 COMMENT '收藏数（冗余计数）',
  `comment_count` INT          NOT NULL DEFAULT 0 COMMENT '评论数（冗余计数）',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章表';

-- ----------------------------
-- 9. 文章-标签关系表
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id`  BIGINT   NOT NULL COMMENT '文章ID → article.id',
  `tag_id`      BIGINT   NOT NULL COMMENT '标签ID → tag.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章-标签关系表';

-- =====================================================================
-- 三、互动相关：comment / like / collect
--    V1 只建表不写代码，留给后续迭代
-- =====================================================================

-- ----------------------------
-- 10. 评论表（两级结构：parent_id = 0 是一级评论，否则是楼中楼回复）
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id`    BIGINT        NOT NULL COMMENT '文章ID → article.id',
  `user_id`       BIGINT        NOT NULL COMMENT '评论人ID → user.id',
  `parent_id`     BIGINT        NOT NULL DEFAULT 0 COMMENT '父评论ID，0 表示一级评论',
  `reply_user_id` BIGINT        DEFAULT NULL COMMENT '被回复人ID（回复某人时记录，方便展示 "A 回复 B"）',
  `content`       VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '审核状态：0待审核 1通过 2驳回',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ----------------------------
-- 11. 点赞表
--     注意：LIKE 是 MySQL 保留字，表名必须用反引号 `like` 包起来，
--     以后写 Java 实体时也要写成 @TableName("`like`")
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id`  BIGINT   NOT NULL COMMENT '文章ID → article.id',
  `user_id`     BIGINT   NOT NULL COMMENT '点赞人ID → user.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`) COMMENT '同一用户对同一文章只能点赞一次',
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章点赞表';

-- ----------------------------
-- 12. 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id`  BIGINT   NOT NULL COMMENT '文章ID → article.id',
  `user_id`     BIGINT   NOT NULL COMMENT '收藏人ID → user.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`) COMMENT '同一用户对同一文章只能收藏一次',
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章收藏表';

-- =====================================================================
-- 四、初始化数据
-- =====================================================================

-- 管理员账号：admin / 123456（密码是 BCrypt 密文，登录时后端用 BCrypt 比对）
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `status`)
VALUES (1, 'admin', '$2a$10$PXqCD2lZnHKA0ldGgbsMY.D/rbifToIyrdRwxEbcBurak8SFTPBKm', '博主', 1);

-- 角色
INSERT INTO `role` (`id`, `role_code`, `role_name`, `sort`, `remark`) VALUES
(1, 'ROLE_ADMIN', '管理员',   1, '拥有后台全部权限'),
(2, 'ROLE_USER',  '普通用户', 2, '可浏览、评论、点赞、收藏');

-- admin 绑定管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 菜单（和前端 V1 的后台路由保持一致）
INSERT INTO `menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(1, 0, '文章管理', 0, '/admin/article',      NULL,                  NULL,           'FileTextOutlined', 1),
(2, 1, '文章列表', 1, '/admin/article/list', 'Article/ArticleList', 'article:list', NULL,               1),
(3, 1, '发布文章', 1, '/admin/article/add',  'Article/ArticleEdit', 'article:add',  NULL,               2);

-- 管理员拥有全部菜单
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES (1, 1), (1, 2), (1, 3);

-- 分类
INSERT INTO `category` (`name`, `description`, `sort`) VALUES
('后端开发', 'Java / Spring Boot 等后端技术', 1),
('前端开发', 'React / Vite 等前端技术',       2),
('生活随笔', '技术之外的记录',                 3);

-- 标签
INSERT INTO `tag` (`name`) VALUES ('Java'), ('Spring Boot'), ('React'), ('MySQL'), ('Redis');
