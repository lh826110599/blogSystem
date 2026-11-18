-- =====================================================================
-- H2 初始化数据（h2 环境专用，后端每次启动自动执行，在 schema-h2.sql 之后）
--
-- 为什么用 INSERT IGNORE + 固定 id？
--   数据库是文件模式，每次启动都会执行本脚本。
--   每行都写死主键 id，第二次启动时主键已存在，INSERT IGNORE 会跳过该行，不会报错也不会重复插入，
--   你在页面上修改过的数据也不会被覆盖。
--   （INSERT IGNORE 在 MySQL 里也能用，H2 的 MySQL 模式兼容这个语法。）
-- 内容和 backend/sql/blog_v1.sql 末尾的初始化数据一致。
-- =====================================================================

-- 管理员账号：admin / 123456（password 是 BCrypt 密文）
INSERT IGNORE INTO `user` (id, username, password, nickname, status) VALUES
(1, 'admin', '$2a$10$PXqCD2lZnHKA0ldGgbsMY.D/rbifToIyrdRwxEbcBurak8SFTPBKm', '博主', 1);

-- 角色
INSERT IGNORE INTO role (id, role_code, role_name, sort, remark) VALUES
(1, 'ROLE_ADMIN', '管理员',   1, '拥有后台全部权限'),
(2, 'ROLE_USER',  '普通用户', 2, '可浏览、评论、点赞、收藏');

-- admin 绑定管理员角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 菜单（和前端后台路由一致）
INSERT IGNORE INTO menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
(1, 0, '文章管理', 0, '/admin/article',      NULL,                  NULL,           'FileTextOutlined', 1),
(2, 1, '文章列表', 1, '/admin/article/list', 'Article/ArticleList', 'article:list', NULL,               1),
(3, 1, '发布文章', 1, '/admin/article/add',  'Article/ArticleEdit', 'article:add',  NULL,               2);

-- 管理员拥有全部菜单
INSERT IGNORE INTO role_menu (id, role_id, menu_id) VALUES (1, 1, 1), (2, 1, 2), (3, 1, 3);

-- 分类
INSERT IGNORE INTO category (id, name, description, sort) VALUES
(1, '后端开发', 'Java / Spring Boot 等后端技术', 1),
(2, '前端开发', 'React / Vite 等前端技术',       2),
(3, '生活随笔', '技术之外的记录',                 3);

-- 标签
INSERT IGNORE INTO tag (id, name) VALUES
(1, 'Java'), (2, 'Spring Boot'), (3, 'React'), (4, 'MySQL'), (5, 'Redis');
