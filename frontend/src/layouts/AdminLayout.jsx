/**
 * 后台管理布局：左侧菜单 + 顶部栏 + 内容区
 *
 *   ┌──────────┬──────────────────────────────┐
 *   │          │  Header（折叠按钮、用户名、退出）│
 *   │  Sider   ├──────────────────────────────┤
 *   │  侧边菜单 │  Content                      │
 *   │          │    <Outlet />  ← 子路由页面渲染在这里 │
 *   └──────────┴──────────────────────────────┘
 *
 * 菜单是写死在前端的。数据库里的 menu 表已经建好，以后做权限时可以改成从后端按角色获取。
 */
import { useEffect, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Avatar, Button, Dropdown, Layout, Menu, theme } from 'antd'
import {
  EditOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UnorderedListOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { getUserInfo, logout } from '@/api/auth'
import { useUserStore } from '@/store/userStore'
import styles from './AdminLayout.module.css'

const { Header, Sider, Content } = Layout

/**
 * 侧边栏菜单配置。key 直接用路由地址，点击菜单时 navigate(key) 就能跳转。
 */
const menuItems = [
  {
    key: '/admin/article',
    icon: <FileTextOutlined />,
    label: '文章管理',
    children: [
      { key: '/admin/article/list', icon: <UnorderedListOutlined />, label: '文章列表' },
      { key: '/admin/article/add', icon: <EditOutlined />, label: '发布文章' },
    ],
  },
]

/**
 * 根据当前地址算出应该高亮的菜单项。
 * 编辑页 /admin/article/edit/1 没有对应的菜单，高亮 "文章列表"。
 */
function getSelectedKey(pathname) {
  if (pathname.startsWith('/admin/article/edit')) {
    return '/admin/article/list'
  }
  return pathname
}

export default function AdminLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const userInfo = useUserStore((state) => state.userInfo)
  const setUserInfo = useUserStore((state) => state.setUserInfo)
  const clearLogin = useUserStore((state) => state.clearLogin)
  // 侧边栏是否折叠
  const [collapsed, setCollapsed] = useState(false)
  // antd 的设计变量（颜色、圆角等），让自定义区域和组件库风格一致
  const { token: designToken } = theme.useToken()

  /**
   * 进入后台时拉取一次用户信息。
   * 作用：1. 刷新本地缓存的昵称等信息；2. 顺便验证 token 是否还有效（失效会返回 401，拦截器自动处理）
   * 依赖数组 [setUserInfo]：Zustand 的 action 引用不会变，所以效果上只在首次挂载时执行
   */
  useEffect(() => {
    getUserInfo()
      .then(setUserInfo)
      .catch(() => {
        // 401 等错误已由拦截器处理
      })
  }, [setUserInfo])

  /**
   * 退出登录：先通知后端删除 Redis 里的 token，无论成功与否都清空本地状态。
   * 清空后 RequireAuth 会自动跳到登录页，这里的 navigate 是保险起见。
   */
  const handleLogout = async () => {
    try {
      await logout()
    } catch {
      // 后端没响应也要能退出
    } finally {
      clearLogin()
      navigate('/login', { replace: true })
    }
  }

  /** 右上角用户下拉菜单 */
  const userMenu = {
    items: [{ key: 'logout', icon: <LogoutOutlined />, label: '退出登录' }],
    onClick: ({ key }) => {
      if (key === 'logout') {
        handleLogout()
      }
    },
  }

  return (
    <Layout className={styles.layout}>
      {/* trigger={null}：不用 Sider 自带的底部折叠按钮，改用顶栏里的按钮 */}
      <Sider trigger={null} collapsible collapsed={collapsed} width={220}>
        <div className={styles.logo}>{collapsed ? 'B' : 'Blog Admin'}</div>
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          selectedKeys={[getSelectedKey(location.pathname)]}
          defaultOpenKeys={['/admin/article']}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>

      <Layout>
        <Header className={styles.header} style={{ background: designToken.colorBgContainer }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <Dropdown menu={userMenu} placement="bottomRight">
            <span className={styles.user}>
              <Avatar size="small" src={userInfo?.avatar} icon={<UserOutlined />} />
              {/* ?. 可选链：userInfo 为 null 时不报错，返回 undefined */}
              <span>{userInfo?.nickname || userInfo?.username}</span>
            </span>
          </Dropdown>
        </Header>

        <Content
          className={styles.content}
          style={{ background: designToken.colorBgContainer, borderRadius: designToken.borderRadiusLG }}
        >
          {/* 子路由（文章列表、发布文章等）渲染在这里 */}
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
