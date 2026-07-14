/**
 * Vue Router 路由配置
 *
 * 定义页面路由：
 *   /login      - 登录页
 *   /register   - 注册页
 *   /files      - 文件管理主页
 *   /           - 用户信息主页
 */

import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import FileManager from '@/views/FileManager.vue'
import UserInfo from '@/views/UserInfo.vue'

// 路由表
const routes = [
  {
    path: '/login',       // 登录页
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',    // 注册页
    name: 'Register',
    component: Register,
  },
  {
    path: '/files',       // 文件管理页
    name: 'FileManager',
    component: FileManager,
  },
  {
    path: '/',            // 用户信息主页（首页）
    name: 'UserInfo',
    component: UserInfo,
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
