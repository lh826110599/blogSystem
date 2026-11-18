/**
 * Axios 请求封装：所有接口请求都通过这里发出。
 *
 * 做了三件事：
 *   1. 统一配置：接口前缀 /api、超时时间
 *   2. 请求拦截器：自动在请求头加上 token
 *   3. 响应拦截器：统一处理后端的 Result 结构和各种错误，页面里只需要关心 "成功拿到的数据"
 *
 * 后端返回格式：{ code: 200, message: '操作成功', data: ... }
 * 经过响应拦截器后，页面里 await 拿到的直接就是 data：
 *   const page = await getArticlePage({ current: 1, size: 10 })   // page = { total, records, ... }
 * 失败时 Promise 会 reject，错误提示已经在拦截器里弹出，页面一般只需要在 finally 里关闭 loading。
 */
import axios from 'axios'
import { message } from 'antd'
import { useUserStore } from '@/store/userStore'

/** 和后端 ResultCode 保持一致的状态码 */
const CODE_SUCCESS = 200
const CODE_UNAUTHORIZED = 401

const request = axios.create({
  // 所有请求自动加上 /api 前缀：request.get('/article/page') 实际请求 /api/article/page，
  // 开发时再由 vite.config.js 的 proxy 转发给后端
  baseURL: '/api',
  // 超时时间 10 秒
  timeout: 10000,
})

/**
 * 请求拦截器：请求发出之前执行。
 * 从 store 里取 token，放到 Authorization 请求头，格式 "Bearer xxx"（和后端 JwtProperties.tokenPrefix 对应）。
 */
request.interceptors.request.use(
  (config) => {
    const { token } = useUserStore.getState()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/**
 * 响应拦截器：收到响应之后、页面拿到数据之前执行。
 *
 * 分两种情况：
 *   1. 成功回调：HTTP 状态码 2xx。后端约定业务错误也返回 HTTP 200，所以还要再看 body 里的 code
 *   2. 失败回调：HTTP 状态码不是 2xx、网络断开、超时、后端没启动（Vite 代理返回 500）等
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 业务成功：只把 data 交给页面
    if (res.code === CODE_SUCCESS) {
      return res.data
    }

    // 未登录 / token 失效：清空登录状态。
    // 不需要在这里写跳转代码：路由守卫 RequireAuth 订阅了 store 里的 token，
    // token 变成 null 后会自动渲染 <Navigate to="/login" />
    if (res.code === CODE_UNAUTHORIZED) {
      useUserStore.getState().clearLogin()
      // 指定 key：同时有多个请求返回 401 时，只显示一条提示，而不是弹出一串
      message.warning({ content: res.message || '登录已失效，请重新登录', key: 'unauthorized' })
      return Promise.reject(new Error(res.message))
    }

    // 其他业务错误（参数校验失败、用户名已存在等）：弹出后端返回的提示
    message.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    let msg = '网络异常，请稍后再试'
    if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请稍后再试'
    } else if (error.response) {
      msg = `请求失败（HTTP ${error.response.status}），请检查后端服务是否启动`
    }
    message.error({ content: msg, key: 'network-error' })
    return Promise.reject(error)
  },
)

export default request
