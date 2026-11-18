/**
 * 根组件：放全局的 "Provider"（给所有子组件提供配置或数据的组件）。
 *
 * 嵌套顺序（从外到内）：
 *   ConfigProvider  → Ant Design 全局配置，这里设置中文语言包（分页 "条/页"、Popconfirm "确定/取消" 等文字）
 *   AntdApp         → Ant Design 的 App 组件，为 message / modal 等提供上下文，并重置一些全局样式
 *   RouterProvider  → React Router，根据当前 URL 渲染 router/index.jsx 里对应的页面组件
 *
 * 说明：项目里直接 import { message } from 'antd' 使用静态方法（Axios 拦截器在组件外，只能用静态方法）。
 * 如果以后给 ConfigProvider 加 theme 主题配置，静态方法拿不到主题，antd 会在控制台给出警告，
 * 届时改用 App.useApp() 或 ConfigProvider.config({ holderRender }) 即可。
 */
import { App as AntdApp, ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { RouterProvider } from 'react-router-dom'
import router from '@/router'

export default function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <AntdApp>
        {/* future 开关：提前启用 React Router v7 的行为，同时消除控制台里的升级提示警告 */}
        <RouterProvider router={router} future={{ v7_startTransition: true }} />
      </AntdApp>
    </ConfigProvider>
  )
}
