/**
 * 应用入口文件
 * 创建 Vue 应用并挂载路由和根组件。
 */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'

// 创建应用实例
const app = createApp(App)

// 使用路由插件
app.use(router)

// 挂载到 index.html 中的 #app 元素
app.mount('#app')
