/**
 * 前端入口文件：把根组件 App 挂载到 index.html 里的 <div id="root">。
 */
import React from 'react'
import ReactDOM from 'react-dom/client'
// Ant Design 的基础样式重置（统一各浏览器的默认 margin、字体等），全局只需引入一次
import 'antd/dist/reset.css'
import './index.css'
import App from './App'

// React 18 的新写法 createRoot（React 17 是 ReactDOM.render）
ReactDOM.createRoot(document.getElementById('root')).render(
  // StrictMode 只在开发环境生效，帮助发现潜在问题。
  // 副作用：开发时组件会 "挂载 → 卸载 → 再挂载" 一次，所以浏览器 Network 里会看到 useEffect 里的接口被调了两次，
  // 这是正常现象，打包上线后只会调用一次
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
