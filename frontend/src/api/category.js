/**
 * 分类接口，对应后端 CategoryController。
 * V1 前端只用到列表（文章页面的分类下拉框），新增 / 修改 / 删除可以在 Knife4j 里调用。
 */
import request from '@/utils/request'

/**
 * 查询全部分类（按 sort 升序）
 * @returns {Promise<Array<{ id: number, name: string, description: string, sort: number }>>}
 */
export const getCategoryList = () => request.get('/category/list')
