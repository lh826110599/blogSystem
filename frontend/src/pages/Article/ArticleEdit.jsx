/**
 * 文章新增 / 编辑页（同一个组件，两个路由共用）
 *
 *   /admin/article/add       → 没有 id，新增模式，表单为空
 *   /admin/article/edit/:id  → 有 id，编辑模式，先查详情回显到表单
 *
 * 两个提交按钮：
 *   "发布"     → status = 1
 *   "存为草稿" → status = 0
 * 因为有两个按钮提交不同的 status，这里不用 htmlType="submit"，而是手动调用 form.validateFields() 校验。
 */
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button, Form, Input, Select, Space, Spin, Switch, Typography, message } from 'antd'
import { createArticle, getArticleDetail, updateArticle } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import styles from './Article.module.css'

/** 文章状态，和后端 article.status 对应 */
const STATUS_DRAFT = 0
const STATUS_PUBLISHED = 1

export default function ArticleEdit() {
  // useParams：取路由里的动态参数 :id，新增页没有这个参数，id 为 undefined
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [form] = Form.useForm()

  const [categories, setCategories] = useState([])
  const [tags, setTags] = useState([])
  // 编辑模式下加载文章详情时的 loading
  const [loading, setLoading] = useState(false)
  // 正在提交的状态值（0 / 1），用来让被点击的那个按钮显示加载中；null 表示没在提交
  const [submittingStatus, setSubmittingStatus] = useState(null)

  // 加载分类和标签下拉选项。Promise.all：两个请求同时发出，都返回后再一起处理
  useEffect(() => {
    Promise.all([getCategoryList(), getTagList()])
      .then(([categoryList, tagList]) => {
        setCategories(categoryList)
        setTags(tagList)
      })
      .catch(() => {})
  }, [])

  /**
   * id 变化时（包括首次进入页面）重置表单；编辑模式下再查询详情回显。
   *
   * 为什么要先 resetFields？从 "编辑文章" 点侧边栏 "发布文章" 时，路由变了但组件是同一个，
   * React 会复用组件实例，不重置的话表单里还留着上一篇文章的内容。
   *
   * ignore 标记：如果请求还没返回就切换了页面，丢弃这次过期的结果，避免把旧数据填进新表单。
   */
  useEffect(() => {
    form.resetFields()
    if (!id) {
      return
    }
    let ignore = false
    setLoading(true)
    getArticleDetail(id)
      .then((article) => {
        if (ignore) return
        // 后端字段 → 表单字段：tags 对象数组转成 id 数组，isTop 的 0/1 转成 Switch 需要的 true/false
        form.setFieldsValue({
          title: article.title,
          categoryId: article.categoryId,
          tagIds: article.tags?.map((tag) => tag.id),
          summary: article.summary,
          cover: article.cover,
          content: article.content,
          isTop: article.isTop === 1,
        })
      })
      .catch(() => {
        // 文章不存在或无权限，提示已由拦截器弹出，回到列表页
        if (!ignore) navigate('/admin/article/list', { replace: true })
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })
    // useEffect 返回的函数在 id 再次变化或组件卸载时执行
    return () => {
      ignore = true
    }
  }, [id, form, navigate])

  /**
   * 提交表单
   * @param {number} status 0 草稿 / 1 发布
   */
  const handleSubmit = async (status) => {
    let values
    try {
      // 校验所有字段，通过后返回表单值；不通过时 antd 会在对应输入框下显示红色提示
      values = await form.validateFields()
    } catch {
      return
    }

    // 表单字段 → 后端 ArticleDTO：Switch 的 true/false 转回 0/1，加上 status
    const payload = { ...values, isTop: values.isTop ? 1 : 0, status }

    setSubmittingStatus(status)
    try {
      if (isEdit) {
        await updateArticle(id, payload)
        message.success('保存成功')
      } else {
        await createArticle(payload)
        message.success(status === STATUS_PUBLISHED ? '发布成功' : '草稿已保存')
      }
      navigate('/admin/article/list')
    } catch {
      // 错误提示已由 Axios 拦截器统一处理
    } finally {
      setSubmittingStatus(null)
    }
  }

  const submitting = submittingStatus !== null

  return (
    <Spin spinning={loading}>
      <Typography.Title level={4}>{isEdit ? '编辑文章' : '发布文章'}</Typography.Title>

      {/* layout="vertical"：标签在输入框上方；initialValues：表单初始值（resetFields 也会恢复成它） */}
      <Form form={form} layout="vertical" initialValues={{ isTop: false }} className={styles.editForm}>
        <Form.Item
          label="标题"
          name="title"
          rules={[
            { required: true, whitespace: true, message: '请输入文章标题' },
            { max: 200, message: '标题最多 200 个字符' },
          ]}
        >
          <Input placeholder="请输入文章标题" maxLength={200} showCount />
        </Form.Item>

        <Form.Item label="分类" name="categoryId" rules={[{ required: true, message: '请选择文章分类' }]}>
          <Select placeholder="请选择分类" options={categories} fieldNames={{ label: 'name', value: 'id' }} />
        </Form.Item>

        <Form.Item label="标签" name="tagIds">
          {/* mode="multiple"：多选，值是 id 数组；maxCount 和后端 ArticleDTO 的 @Size(max = 10) 一致 */}
          <Select
            mode="multiple"
            placeholder="请选择标签（选填，最多 10 个）"
            allowClear
            maxCount={10}
            options={tags}
            fieldNames={{ label: 'name', value: 'id' }}
          />
        </Form.Item>

        <Form.Item label="摘要" name="summary" rules={[{ max: 500, message: '摘要最多 500 个字符' }]}>
          <Input.TextArea rows={2} maxLength={500} showCount placeholder="选填，不填时自动截取正文前 100 个字" />
        </Form.Item>

        <Form.Item
          label="封面图"
          name="cover"
          rules={[
            { type: 'url', message: '请输入正确的图片地址，如 https://example.com/a.png' },
            { max: 255, message: '地址最多 255 个字符' },
          ]}
        >
          <Input placeholder="选填，图片的网络地址" />
        </Form.Item>

        <Form.Item
          label="正文"
          name="content"
          rules={[{ required: true, whitespace: true, message: '请输入文章内容' }]}
        >
          <Input.TextArea rows={16} placeholder="支持 Markdown 语法" className={styles.content} />
        </Form.Item>

        {/* Switch 的值属性叫 checked 而不是 value，用 valuePropName 告诉 Form.Item */}
        <Form.Item label="置顶" name="isTop" valuePropName="checked">
          <Switch />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button
              type="primary"
              loading={submittingStatus === STATUS_PUBLISHED}
              disabled={submitting}
              onClick={() => handleSubmit(STATUS_PUBLISHED)}
            >
              发布
            </Button>
            <Button
              loading={submittingStatus === STATUS_DRAFT}
              disabled={submitting}
              onClick={() => handleSubmit(STATUS_DRAFT)}
            >
              存为草稿
            </Button>
            <Button disabled={submitting} onClick={() => navigate('/admin/article/list')}>
              取消
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Spin>
  )
}
