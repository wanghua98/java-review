<!--
  根组件
  顶部导航栏：未登录时为空，登录后右侧显示用户昵称 + 退出登录。
  通过 auth store 控制登录状态，页面加载时自动验证会话。
-->
<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { initAuth, useAuth, logout } from '@/stores/auth.js'

const router = useRouter()
const { user } = useAuth()

/** 页面启动时验证当前会话 */
onMounted(async () => {
  const loggedIn = await initAuth()
  if (!loggedIn) {
    router.push('/login')
  } else if (router.currentRoute.value.name === 'Login' || router.currentRoute.value.name === 'Register') {
    router.push('/files')
  }
})

/** 退出登录 */
async function handleLogout() {
  await logout()
  router.push('/login')
}

/** 跳转到个人信息页 */
function goToUserInfo() {
  router.push('/user-info')
}

/** 跳转到文件管理 */
function goToFiles() {
  router.push('/files')
}
</script>

<template>
  <div id="app-root">
    <!-- 顶部导航栏 -->
    <nav>
      <div class="nav-left" @click="goToFiles" style="cursor:pointer;">
        <span class="nav-title">📁 文件管理</span>
      </div>
      <div class="nav-right" v-if="user">
        <span class="nav-nickname" @click="goToUserInfo" title="修改个人信息">
          {{ user.nickname || user.username }}
        </span>
        <button class="nav-logout" @click="handleLogout">退出登录</button>
      </div>
    </nav>

    <!-- 路由页面渲染位置 -->
    <router-view />
  </div>
</template>

<style scoped>
nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 48px;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.nav-left {
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-nickname {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.nav-nickname:hover {
  background: #ecf5ff;
  text-decoration: underline;
}

.nav-logout {
  font-size: 13px;
  color: #999;
  background: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.nav-logout:hover {
  color: #f56c6c;
  border-color: #f56c6c;
}
</style>
