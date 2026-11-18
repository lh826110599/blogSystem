/**
 * 认证相关接口，对应后端 AuthController。
 *
 * api 目录的作用：把 "接口地址 + 请求方式" 集中管理，页面里只调用函数，不写 URL。
 * 后端改了接口路径，只需要改这里一处。
 */
import request from '@/utils/request'

/**
 * 登录
 * @param {{ username: string, password: string }} data
 * @returns {Promise<{ token: string, userInfo: object }>}
 */
export const login = (data) => request.post('/auth/login', data)

/**
 * 退出登录（后端会删除 Redis 里的 token，旧 token 立即失效）
 */
export const logout = () => request.post('/auth/logout')

/**
 * 获取当前登录用户信息
 * @returns {Promise<{ id: number, username: string, nickname: string, avatar: string, email: string }>}
 */
export const getUserInfo = () => request.get('/auth/info')
