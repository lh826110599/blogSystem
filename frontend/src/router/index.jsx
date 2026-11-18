/**
 * 路由配置（React Router v6 数据路由写法：createBrowserRouter + RouterProvider）
 *
 * 路由表：
 *   /login                    登录页
 *   /admin                    后台布局（需要登录），本身不显示内容，自动跳到文章列表
 *   ├─ /admin/article/list    文章列表
 *   ├─ /admin/article/add     发布文章
 *   └─ /admin/article/edit/:id 编辑文章（和发布共用 ArticleEdit 组件）
 *   /                         跳到 /admin
 *   其他任何地址              404
 *
 * 嵌套路由：children 里的页面会渲染在父组件 AdminLayout 的 <Outlet /> 位置，
 * 所以切换文章列表 / 发布文章时，侧边栏和顶栏不会重新渲染。
 */
import { Button, Result } from 'antd'
import { Link, Navigate, createBrowserRouter } from 'react-router-dom'
import RequireAuth from './RequireAuth'
import AdminLayout from '@/layouts/AdminLayout'
import Login from '@/pages/Login'
import ArticleList from '@/pages/Article/ArticleList'
import ArticleEdit from '@/pages/Article/ArticleEdit'

const router = createBrowserRouter(
  [
    {
      path: '/login',
      element: <Login />,
    },
    {
      path: '/admin',
      // 用 RequireAuth 包住整个后台布局，后台下的所有子页面都需要登录
      element: (
        <RequireAuth>
          <AdminLayout />
        </RequireAuth>
      ),
      children: [
        // index 路由：访问 /admin 本身时渲染，这里直接重定向到文章列表
        { index: true, element: <Navigate to="article/list" replace /> },
        // 子路由的 path 不以 / 开头，表示相对父路由：article/list → /admin/article/list
        { path: 'article/list', element: <ArticleList /> },
        { path: 'article/add', element: <ArticleEdit /> },
        // :id 是动态参数，在组件里用 useParams() 取出
        { path: 'article/edit/:id', element: <ArticleEdit /> },
      ],
    },
    {
      path: '/',
      element: <Navigate to="/admin" replace />,
    },
    {
      // * 匹配上面都没匹配到的地址
      path: '*',
      element: (
        <Result
          status="404"
          title="404"
          subTitle="页面不存在"
          extra={
            <Link to="/">
              <Button type="primary">返回首页</Button>
            </Link>
          }
        />
      ),
    },
  ],
  {
    // 提前启用 React Router v7 的行为，同时消除控制台里的升级提示警告
    future: {
      v7_relativeSplatPath: true,
      v7_fetcherPersist: true,
      v7_normalizeFormMethod: true,
      v7_partialHydration: true,
      v7_skipActionErrorRevalidation: true,
    },
  },
)

export default router
