/**
 * 登录守卫：包在需要登录才能访问的页面外面。
 *
 *   有 token  → 正常渲染子组件（后台布局）
 *   没 token  → 跳转到 /login，并通过 state 记住原来想去的页面，登录成功后跳回来
 *
 * 为什么 Axios 拦截器收到 401 后不用自己写跳转？
 * 这里用 useUserStore((s) => s.token) 订阅了 token，拦截器调用 clearLogin() 把 token 清空后，
 * 本组件会自动重新渲染，走到下面的 <Navigate>。跳转逻辑只写在这一处。
 *
 * 注意：这只是前端体验上的拦截，真正的安全校验在后端 LoginInterceptor。
 * 前端代码在用户浏览器里运行，可以被随意修改，绕过前端守卫也拿不到后端数据。
 */
import { Navigate, useLocation } from 'react-router-dom'
import { useUserStore } from '@/store/userStore'

export default function RequireAuth({ children }) {
  const token = useUserStore((state) => state.token)
  const location = useLocation()

  if (!token) {
    // replace：替换当前历史记录，登录后点浏览器 "后退" 不会又回到这个被拦截的地址
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return children
}
