/**
 * 登录页
 *
 * 流程：填写表单 → antd Form 校验 → 调用登录接口 → token 存入 Zustand → 跳转到后台
 *
 * 知识点：
 *   - antd Form：rules 做前端校验，onFinish 只有校验全部通过才会触发
 *   - useNavigate：在代码里跳转路由
 *   - useLocation().state：读取 RequireAuth 跳转时带过来的 "原本想访问的页面"
 */
import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { Button, Card, Form, Input, Typography, message } from 'antd'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/userStore'
import styles from './index.module.css'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const token = useUserStore((state) => state.token)
  const setLogin = useUserStore((state) => state.setLogin)
  // 登录按钮的加载状态：请求期间按钮转圈并禁止重复点击
  const [loading, setLoading] = useState(false)

  // 登录后要回到的页面：被 RequireAuth 拦截时记录的地址，没有就去后台首页
  const redirectTo = location.state?.from || '/admin'

  // 已经登录过（localStorage 里有 token）就不用再显示登录页
  if (token) {
    return <Navigate to={redirectTo} replace />
  }

  /**
   * 表单提交（前端校验通过后才会执行）
   * @param {{ username: string, password: string }} values 表单里所有字段的值，key 是 Form.Item 的 name
   */
  const handleFinish = async (values) => {
    setLoading(true)
    try {
      const data = await login(values)
      setLogin(data)
      message.success('登录成功')
      // replace：登录页不留在历史记录里，进入后台后点 "后退" 不会回到登录页
      navigate(redirectTo, { replace: true })
    } catch {
      // 用户名或密码错误等提示，已经由 Axios 响应拦截器统一弹出，这里不用重复处理
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <Card className={styles.card}>
        <Typography.Title level={3} className={styles.title}>
          博客后台管理
        </Typography.Title>

        <Form size="large" autoComplete="off" onFinish={handleFinish}>
          {/* name 对应提交给后端的字段名；rules 是前端校验规则，和后端 LoginDTO 的校验保持一致 */}
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item>
            {/* htmlType="submit"：点击时触发表单校验和 onFinish，在输入框里按回车也会触发 */}
            <Button type="primary" htmlType="submit" block loading={loading}>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
