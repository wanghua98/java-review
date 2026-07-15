import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: true,         // 监听所有网卡地址（支持局域网访问）
    port: 80,         // 默认端口
    // API 代理：将 /api 请求转发到后端
    proxy: {
      '/api': {
        target: 'http://localhost:8001',
        changeOrigin: false,
      },
    },
  },
})
