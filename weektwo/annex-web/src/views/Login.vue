<!--
  登录页面
  用户输入用户名和密码后调用后端登录接口。
  登录成功后设置全局登录状态并跳转到文件管理页。
-->
<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {login} from '@/api/user.js'
import {setUser} from '@/stores/auth.js'

const router = useRouter()

// 表单数据
const username = ref('')
const password = ref('')
// 提示消息
const message = ref('')
// 加载状态
const loading = ref(false)

/**
 * 点击登录按钮时触发
 * 调用后端登录接口，成功则更新全局状态并跳转到文件管理
 */
async function handleLogin() {
  // 简单校验：用户名和密码不能为空
  if (!username.value || !password.value) {
    message.value = '用户名和密码不能为空'
    return
  }
  loading.value = true
  message.value = ''
  // 调用登录 API
  const res = await login(username.value, password.value)
  if (res && res.code === 200) {
    // 先获取用户信息更新全局状态
    const {getUserInfo} = await import('@/api/user.js')
    const infoRes = await getUserInfo()
    if (infoRes.code === 200 && infoRes.data) {
      setUser(infoRes.data)
    }
    // 跳转到文件管理
    router.push('/files')
  } else {
    // 登录失败，显示后端返回的错误信息
    message.value = res.message || '登录失败'
  }
  loading.value = false
}
</script>

<template>
  <div class="page">
    <h2>登录</h2>
    <!-- 表单 -->
    <div class="form">
      <input v-model="username" placeholder="用户名"/>
      <input v-model="password" type="password" placeholder="密码"/>
      <button @click="handleLogin" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
    </div>
    <!-- 提示消息 -->
    <p v-if="message" class="msg">{{ message }}</p>
    <!-- 跳转注册 -->
    <p>还没有账号？
      <router-link to="/register">去注册</router-link>
    </p>
  </div>
</template>

<style scoped>
.page {
  max-width: 360px;
  margin: 80px auto;
  text-align: center;
}

h2 {
  margin-bottom: 20px;
  font-size: 20px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

input {
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
}

input:focus {
  border-color: #409eff;
}

button {
  padding: 8px;
  cursor: pointer;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
}

button:hover {
  background: #66b1ff;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.msg {
  color: red;
  font-size: 13px;
}
</style>
