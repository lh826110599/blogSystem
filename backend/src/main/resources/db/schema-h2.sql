-- =====================================================================
-- H2 建表脚本（h2 环境专用，后端每次启动自动执行，见 application-h2.yml）
--
-- 表结构和 backend/sql/blog_v1.sql（MySQL 版）一致，写法上的区别：
--   1. 用 CREATE TABLE IF NOT EXISTS，不 DROP：数据存在文件里，重启后要保留
--   2. 去掉 ENGINE / CHARSET / COLLATE / 表注释等 MySQL 专有写法，H2 解析不了
--   3. H2 的约束名、索引名在整个库里必须唯一（MySQL 只要求单表内唯一），所以都加了表名前缀
--   4. 大文本类型 LONGTEXT 换成 H2 的 CLOB
--   5. 不写列 COMMENT，字段含义看 MySQL 版脚本
--
-- ⚠ 以后改表结构，两个脚本要一起改。
-- =====================================================================

-- ---------- 1. 用户表 ----------
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50),
    avatar      VARCHAR(255),
    email       VARCHAR(100),
    status      TINYINT      DEFAULT 1 NOT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted     TINYINT      DEFAULT 0 NOT NULL,
    CONSTRAINT uk_user_username UNIQUE (username)
);

-- ---------- 2. 角色表 ----------
CREATE TABLE IF NOT EXISTS role (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    role_code   VARCHAR(50)  NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    sort        INT          DEFAULT 0 NOT NULL,
    status      TINYINT      DEFAULT 1 NOT NULL,
    remark      VARCHAR(255),
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted     TINYINT      DEFAULT 0 NOT NULL,
    CONSTRAINT uk_role_code UNIQUE (role_code)
);

-- ---------- 3. 菜单表 ----------
CREATE TABLE IF NOT EXISTS menu (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0 NOT NULL,
    menu_name   VARCHAR(50)  NOT NULL,
    menu_type   TINYINT      DEFAULT 1 NOT NULL,
    path        VARCHAR(200),
    component   VARCHAR(200),
    perms       VARCHAR(100),
    icon        VARCHAR(100),
    sort        INT          DEFAULT 0 NOT NULL,
    visible     TINYINT      DEFAULT 1 NOT NULL,
    status      TINYINT      DEFAULT 1 NOT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted     TINYINT      DEFAULT 0 NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON menu (parent_id);

-- ---------- 4. 用户-角色关系表 ----------
CREATE TABLE IF NOT EXISTS user_role (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    role_id     BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role (role_id);

-- ---------- 5. 角色-菜单关系表 ----------
CREATE TABLE IF NOT EXISTS role_menu (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    role_id     BIGINT   NOT NULL,
    menu_id     BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_role_menu UNIQUE (role_id, menu_id)
);
CREATE INDEX IF NOT EXISTS idx_role_menu_menu_id ON role_menu (menu_id);

-- ---------- 6. 分类表 ----------
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    sort        INT          DEFAULT 0 NOT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted     TINYINT      DEFAULT 0 NOT NULL,
    CONSTRAINT uk_category_name UNIQUE (name)
);

-- ---------- 7. 标签表 ----------
CREATE TABLE IF NOT EXISTS tag (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted     TINYINT     DEFAULT 0 NOT NULL,
    CONSTRAINT uk_tag_name UNIQUE (name)
);

-- ---------- 8. 文章表 ----------
CREATE TABLE IF NOT EXISTS article (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    summary       VARCHAR(500),
    content       CLOB         NOT NULL,
    cover         VARCHAR(255),
    category_id   BIGINT       NOT NULL,
    author_id     BIGINT       NOT NULL,
    status        TINYINT      DEFAULT 1 NOT NULL,
    is_top        TINYINT      DEFAULT 0 NOT NULL,
    view_count    INT          DEFAULT 0 NOT NULL,
    like_count    INT          DEFAULT 0 NOT NULL,
    collect_count INT          DEFAULT 0 NOT NULL,
    comment_count INT          DEFAULT 0 NOT NULL,
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted       TINYINT      DEFAULT 0 NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_article_category_id ON article (category_id);
CREATE INDEX IF NOT EXISTS idx_article_author_id ON article (author_id);
CREATE INDEX IF NOT EXISTS idx_article_status_create_time ON article (status, create_time);

-- ---------- 9. 文章-标签关系表 ----------
CREATE TABLE IF NOT EXISTS article_tag (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT   NOT NULL,
    tag_id      BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_article_tag UNIQUE (article_id, tag_id)
);
CREATE INDEX IF NOT EXISTS idx_article_tag_tag_id ON article_tag (tag_id);

-- ---------- 10. 评论表 ----------
CREATE TABLE IF NOT EXISTS comment (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    article_id    BIGINT        NOT NULL,
    user_id       BIGINT        NOT NULL,
    parent_id     BIGINT        DEFAULT 0 NOT NULL,
    reply_user_id BIGINT,
    content       VARCHAR(1000) NOT NULL,
    status        TINYINT       DEFAULT 1 NOT NULL,
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    deleted       TINYINT       DEFAULT 0 NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_comment_article_id ON comment (article_id);
CREATE INDEX IF NOT EXISTS idx_comment_user_id ON comment (user_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent_id ON comment (parent_id);

-- ---------- 11. 点赞表（LIKE 在 H2 和 MySQL 里都是关键字，必须加反引号） ----------
CREATE TABLE IF NOT EXISTS `like` (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_like_user_article UNIQUE (user_id, article_id)
);
CREATE INDEX IF NOT EXISTS idx_like_article_id ON `like` (article_id);

-- ---------- 12. 收藏表 ----------
CREATE TABLE IF NOT EXISTS collect (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_collect_user_article UNIQUE (user_id, article_id)
);
CREATE INDEX IF NOT EXISTS idx_collect_article_id ON collect (article_id);
