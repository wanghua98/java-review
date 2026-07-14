<!--
  注册页面
  用户输入用户名和密码后调用后端注册接口。
  注册成功后自动跳转到登录页，让用户手动登录。
-->
<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {register} from '@/api/user.js'

const router = useRouter()

// 表单数据
const username = ref('')
const password = ref('')
// 提示消息
const message = ref('')
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
  // 调用注册 API
  const res = await register(username.value, password.value)
  if (res.code === 200) {
    // 注册成功，后跳转到我的文件解密页面
    success.value = true
    message.value = '注册成功，即将跳转到我的文件界面'
    setTimeout(() => router.push('/files'), 500)
  } else {
    message.value = res.message || '注册失败'
  }
}
</script>

<template>
  <div class="page">
    <h2>注册</h2>
    <div class="form">
      <input v-model="username" placeholder="用户名"/>
      <input v-model="password" type="password" placeholder="密码"/>
      <button @click="handleRegister">注册</button>
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

.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

input {
  padding: 8px 12px;
  font-size: 14px;
}

button {
  padding: 8px;
  cursor: pointer;
}

.msg {
  color: red;
  font-size: 13px;
}

.msg.success {
  color: green;
}
</style>
