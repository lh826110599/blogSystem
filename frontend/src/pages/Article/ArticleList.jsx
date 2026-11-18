/**
 * 文章列表页
 *
 * 页面结构：上方筛选栏（标题关键字 / 分类 / 状态）+ 表格（服务端分页）
 *
 * 数据流（React 的 "状态驱动" 思路）：
 *   用户操作（查询、翻页） → 修改 query 状态 → useEffect 监听到 query 变化 → 重新请求接口 → 更新 data → 表格重新渲染
 * 所有 "重新查询" 都只需要改 query，不用在每个按钮里手动调接口。
 */
import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Form, Input, Popconfirm, Select, Space, Table, Tag, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { deleteArticle, getArticlePage } from '@/api/article'
import { getCategoryList } from '@/api/category'
import styles from './Article.module.css'

/** 文章状态，和后端 article.status 对应 */
const STATUS_OPTIONS = [
  { label: '已发布', value: 1 },
  { label: '草稿', value: 0 },
]

/** 默认分页参数 */
const DEFAULT_PAGE = { current: 1, size: 10 }

export default function ArticleList() {
  const navigate = useNavigate()
  // Form.useForm()：拿到表单实例，可以在代码里重置、读取、设置表单值
  const [form] = Form.useForm()

  // 查询条件（分页 + 筛选），改变它就会触发重新查询
  const [query, setQuery] = useState(DEFAULT_PAGE)
  // 接口返回的分页数据
  const [pageData, setPageData] = useState({ total: 0, records: [] })
  const [loading, setLoading] = useState(false)
  // 分类下拉框的选项
  const [categories, setCategories] = useState([])

  // 页面加载时查一次分类，空依赖数组 [] 表示只在组件首次挂载时执行
  useEffect(() => {
    getCategoryList()
      .then(setCategories)
      .catch(() => {})
  }, [])

  /**
   * 查询文章列表。
   * useCallback：只有 query 变化时才生成新的函数，配合下面的 useEffect 实现 "query 变了就重新查"
   */
  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const data = await getArticlePage(query)
      setPageData(data)
    } catch {
      // 错误提示已由 Axios 拦截器统一处理
    } finally {
      setLoading(false)
    }
  }, [query])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  /** 点击 "查询"：带上筛选条件，回到第 1 页 */
  const handleSearch = (values) => {
    setQuery({ ...values, current: 1, size: query.size })
  }

  /** 点击 "重置"：清空表单和筛选条件 */
  const handleReset = () => {
    form.resetFields()
    setQuery({ ...DEFAULT_PAGE, size: query.size })
  }

  /** 表格翻页 / 修改每页条数 */
  const handleTableChange = (pagination) => {
    // 函数式更新 setQuery(prev => ...)：基于最新的 query 修改，保留筛选条件
    setQuery((prev) => ({ ...prev, current: pagination.current, size: pagination.pageSize }))
  }

  /**
   * 删除文章。返回 Promise 给 Popconfirm，删除期间确认按钮会显示加载状态。
   * 如果删的是当前页最后一条，且不在第 1 页，就回到上一页，避免显示空页面。
   */
  const handleDelete = async (id) => {
    try {
      await deleteArticle(id)
      message.success('删除成功')
      if (pageData.records.length === 1 && query.current > 1) {
        setQuery((prev) => ({ ...prev, current: prev.current - 1 }))
      } else {
        fetchList()
      }
    } catch {
      // 错误提示已由 Axios 拦截器统一处理
    }
  }

  /**
   * 表格列配置
   *   dataIndex：从每行数据里取哪个字段显示
   *   render：自定义显示内容，参数是 (当前字段值, 整行数据)
   */
  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      ellipsis: true,
      render: (title, record) => (
        <Space size={4}>
          {record.isTop === 1 && <Tag color="red">置顶</Tag>}
          {title}
        </Space>
      ),
    },
    { title: '分类', dataIndex: 'categoryName', width: 120 },
    {
      title: '标签',
      dataIndex: 'tags',
      width: 220,
      // antd 6 的 Tag 没有默认外边距，用 Space 控制间距；wrap：标签多时自动换行
      // 渲染列表时每一项都要有唯一的 key，React 靠它判断哪一项变了
      render: (tags) => (
        <Space size={[4, 4]} wrap>
          {tags?.map((tag) => (
            <Tag key={tag.id} color="blue">
              {tag.name}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (status) => (status === 1 ? <Tag color="green">已发布</Tag> : <Tag>草稿</Tag>),
    },
    { title: '浏览量', dataIndex: 'viewCount', width: 90 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
    {
      title: '操作',
      key: 'action',
      width: 140,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => navigate(`/admin/article/edit/${record.id}`)}>
            编辑
          </Button>
          <Popconfirm title="确定删除这篇文章吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <>
      <div className={styles.toolbar}>
        {/* layout="inline"：表单项横向排成一行 */}
        <Form form={form} layout="inline" onFinish={handleSearch}>
          <Form.Item name="keyword">
            <Input placeholder="标题关键字" allowClear />
          </Form.Item>
          <Form.Item name="categoryId">
            {/* fieldNames：告诉 Select 用数据里的 name 当显示文字、id 当值，不用先 map 成 { label, value } */}
            <Select
              placeholder="全部分类"
              allowClear
              options={categories}
              fieldNames={{ label: 'name', value: 'id' }}
              className={styles.select}
            />
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="全部状态" allowClear options={STATUS_OPTIONS} className={styles.select} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>

        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/article/add')}>
          发布文章
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={pageData.records}
        loading={loading}
        scroll={{ x: 1000 }}
        onChange={handleTableChange}
        // 服务端分页：当前页、每页条数、总数都由我们传给表格，而不是让表格自己对全部数据分页
        pagination={{
          current: query.current,
          pageSize: query.size,
          total: pageData.total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
      />
    </>
  )
}
