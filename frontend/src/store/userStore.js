/**
 * 用户登录状态（Zustand）
 *
 * 为什么需要全局状态？token 和用户信息在很多地方都要用：
 *   - Axios 请求拦截器：每次请求带上 token
 *   - 路由守卫 RequireAuth：没有 token 就跳登录页
 *   - 后台布局右上角：显示昵称
 * 放进全局 store，各处直接读取，不用一层层通过 props 传递。
 *
 * Zustand 用法：
 *   在组件里（会随数据变化自动重新渲染）：
 *     const token = useUserStore((state) => state.token)
 *   在组件外（比如 Axios 拦截器，只读一次当前值）：
 *     const { token } = useUserStore.getState()
 *
 * persist 中间件：自动把 state 同步到 localStorage（key = 'blog-user'），
 * 刷新页面后自动恢复，用户不用重新登录。在浏览器 F12 → Application → Local Storage 里能看到。
 */
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useUserStore = create(
  persist(
    (set) => ({
      /** JWT token（不带 Bearer 前缀），未登录时为 null */
      token: null,

      /** 当前用户信息：{ id, username, nickname, avatar, email } */
      userInfo: null,

      /**
       * 登录成功后保存 token 和用户信息
       * @param {{ token: string, userInfo: object }} loginData 后端 /auth/login 返回的 data
       */
      setLogin: ({ token, userInfo }) => set({ token, userInfo }),

      /** 更新用户信息（进入后台时重新拉取一次，保证昵称等是最新的） */
      setUserInfo: (userInfo) => set({ userInfo }),

      /**
       * 清除登录状态：退出登录、token 失效时调用。
       * 清空后 RequireAuth 会自动检测到 token 变成 null，跳转到登录页
       */
      clearLogin: () => set({ token: null, userInfo: null }),
    }),
    {
      // localStorage 中的 key
      name: 'blog-user',
      // 只持久化数据字段（函数本来也存不进去，这里写明更清楚）
      partialize: (state) => ({ token: state.token, userInfo: state.userInfo }),
    },
  ),
)
