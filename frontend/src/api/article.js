/**
 * 文章接口，对应后端 ArticleController。
 */
import request from '@/utils/request'

/**
 * 分页查询文章
 *
 * GET 请求的参数要放在 params 里，axios 会拼成查询字符串：/article/page?current=1&size=10&keyword=xx
 * 值为 undefined 或 null 的参数会被自动丢弃，所以没选的筛选条件不会发给后端。
 *
 * @param {{ current: number, size: number, keyword?: string, categoryId?: number, status?: number }} params
 * @returns {Promise<{ total: number, current: number, size: number, records: Array<object> }>}
 */
export const getArticlePage = (params) => request.get('/article/page', { params })

/**
 * 文章详情（包含正文 content 和标签 tags），编辑页回显用
 * @param {number|string} id 文章ID
 */
export const getArticleDetail = (id) => request.get(`/article/${id}`)

/**
 * 发布文章
 * POST 请求的第二个参数是请求体，axios 会自动转成 JSON 并设置 Content-Type: application/json
 *
 * @param {{ title: string, content: string, categoryId: number, tagIds?: number[],
 *           summary?: string, cover?: string, status: 0|1, isTop?: 0|1 }} data
 * @returns {Promise<number>} 新文章ID
 */
export const createArticle = (data) => request.post('/article', data)

/**
 * 修改文章（标签按 tagIds 整体替换）
 * @param {number|string} id 文章ID
 * @param {object} data 字段同 createArticle
 */
export const updateArticle = (id, data) => request.put(`/article/${id}`, data)

/**
 * 删除文章
 * @param {number} id 文章ID
 */
export const deleteArticle = (id) => request.delete(`/article/${id}`)
