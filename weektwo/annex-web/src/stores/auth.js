/**
 * 用户认证状态管理
 *
 * 提供全局的登录状态和用户信息，供导航栏和路由守卫使用。
 * 通过 getUserInfo() 验证登录状态，在登录/登出时更新。
 */
import { ref } from 'vue'
import { getUserInfo, logout as apiLogout } from '@/api/user.js'

/** 当前登录用户信息（{ username, nickname, role }），null 表示未登录 */
const user = ref(null)

/** 供路由守卫等需要直接引用 user 的地方使用 */
export { user }

/** 是否已尝试过后端验证 */
const initialized = ref(false)

/**
 * 初始化认证状态：调用 /api/info 验证当前会话
 * 建议在 App.vue 的 onMounted 中调用一次
 */
export async function initAuth() {
  const res = await getUserInfo()
  if (res.code === 200 && res.data) {
    user.value = res.data
  } else {
    user.value = null
  }
  initialized.value = true
  return !!user.value
}

/** 设置登录用户 */
export function setUser(data) {
  user.value = data
}

/** 退出登录 */
export async function logout() {
  await apiLogout()
  user.value = null
}

/**
 * 返回响应式引用
 */
export function useAuth() {
  return { user, initialized }
}
