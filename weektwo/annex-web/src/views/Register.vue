<!--
  注册页面
  用户输入用户名和密码后调用后端注册接口。
  注册成功后自动登录（后端已处理），更新全局状态并跳转。
-->
<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {register} from '@/api/user.js'
import {setUser} from '@/stores/auth.js'

const router = useRouter()

// 表单数据
const username = ref('')
const password = ref('')
// 提示消息
const message = ref('')
// 加载状态
const loading = ref(false)
// 是否注册成功（用于显示不同提示）
const success = ref(false)

/**
 * 点击注册按钮时触发
 */
async function handleRegister() {
  // 简单校验
  if (!username.value || !password.value) {
    message.value = '用户名和密码不能为空'
    return
  }
  loading.value = true
  message.value = ''
  // 调用注册 API
  const res = await register(username.value, password.value)
  if (res.code === 200) {
    // 注册成功，后端已自动登录，获取用户信息更新全局状态
    success.value = true
    message.value = '注册成功，即将跳转'
    const {getUserInfo} = await import('@/api/user.js')
    const infoRes = await getUserInfo()
    if (infoRes.code === 200 && infoRes.data) {
      setUser(infoRes.data)
    }
    setTimeout(() => router.push('/files'), 500)
  } else {
    message.value = res.message || '注册失败'
  }
  loading.value = false
}
</script>

<template>
  <div class="page">
    <h2>注册</h2>
    <div class="form">
      <input v-model="username" placeholder="用户名（字母开头，字母数字）"/>
      <input v-model="password" type="password" placeholder="密码"/>
      <button @click="handleRegister" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
    </div>
    <!-- 提示消息，成功后显示绿色 -->
    <p v-if="message" :class="['msg', { success }]">{{ message }}</p>
    <p>已有账号？
      <router-link to="/login">去登录</router-link>
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

.msg.success {
  color: green;
}
</style>
