import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Vite 配置
 * 文档：https://vite.dev/config/
 */
export default defineConfig({
  // React 插件：支持 JSX 语法和开发时的热更新（改代码后页面局部刷新，组件状态不丢）
  plugins: [react()],

  resolve: {
    alias: {
      // 路径别名：import request from '@/utils/request' 等价于 '<项目根>/src/utils/request'
      // 不用再写 '../../utils/request' 这种层层往上找的相对路径
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  server: {
    port: 5173,
    // 开发代理：浏览器请求 http://localhost:5173/api/xxx 时，由 Vite 开发服务器转发到后端 http://localhost:8080/api/xxx
    // 对浏览器来说请求的是同一个源（5173），所以不会触发跨域限制
    // 后端配置了 context-path: /api，路径原样转发即可，不需要 rewrite 去掉 /api 前缀
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  build: {
    // Ant Design 组件库本身体积较大，打包后的 JS 会超过 Vite 默认的 500 KB 提示阈值。
    // 后台管理系统对首屏体积不敏感，这里调高阈值，避免每次打包都输出警告
    chunkSizeWarningLimit: 1500,
  },
})
