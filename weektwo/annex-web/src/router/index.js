/**
 * Vue Router 路由配置
 *
 * 路由指南：
 *   /login      - 登录页（未登录自动跳转）
 *   /register   - 注册页
 *   /files      - 文件管理主页
 *   /user-info  - 用户信息页（修改昵称、密码）
 */

import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import FileManager from '@/views/FileManager.vue'
import UserInfo from '@/views/UserInfo.vue'

// 引入认证状态（ref 是响应式的，跨模块共享同一实例）
import { user } from '@/stores/auth.js'

// 需要登录才能访问的路由
const protectedRoutes = ['FileManager', 'UserInfo']

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  },
  {
    path: '/files',
    name: 'FileManager',
    component: FileManager,
  },
  {
    path: '/user-info',
    name: 'UserInfo',
    component: UserInfo,
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 路由导航守卫
 * - 未登录访问受保护页面 → 重定向到 /login
 * - 已登录访问 /login 或 /register → 重定向到 /files
 */
router.beforeEach((to, from, next) => {
  const isProtected = protectedRoutes.includes(to.name)
  const loggedIn = !!user.value

  if (isProtected && !loggedIn) {
    // 未登录 → 登录页
    next({ name: 'Login' })
  } else if (loggedIn && (to.name === 'Login' || to.name === 'Register')) {
    // 已登录 → 直接去文件管理
    next({ name: 'FileManager' })
  } else {
    next()
  }
})

export default router
