/**
 * 标签接口，对应后端 TagController。
 * V1 前端只用到列表（文章新增页面的标签多选框），新增 / 修改 / 删除可以在 Knife4j 里调用。
 */
import request from '@/utils/request'

/**
 * 查询全部标签
 * @returns {Promise<Array<{ id: number, name: string }>>}
 */
export const getTagList = () => request.get('/tag/list')
