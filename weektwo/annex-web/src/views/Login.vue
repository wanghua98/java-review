<!--
  登录页面
  用户输入用户名和密码后调用后端登录接口。
  登录成功后跳转到用户信息主页（/）。
-->
<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {login} from '@/api/user.js'

const router = useRouter()

// 表单数据
const username = ref('')
const password = ref('')
// 提示消息
const message = ref('')

/**
 * 点击登录按钮时触发
 * 调用后端登录接口，成功则跳转到首页
 */
async function handleLogin() {
  // 简单校验：用户名和密码不能为空
  if (!username.value || !password.value) {
    message.value = '用户名和密码不能为空'
    return
  }
  // 调用登录 API
  const res = await login(username.value, password.value)
  if (res.code === 200) {
    // 登录成功，跳转到首页
    router.push('/')
  } else {
    // 登录失败，显示后端返回的错误信息
    message.value = res.message || '登录失败'
  }
}
</script>

<template>
  <div class="page">
    <h2>登录</h2>
    <!-- 表单 -->
    <div class="form">
      <input v-model="username" placeholder="用户名"/>
      <input v-model="password" type="password" placeholder="密码"/>
      <button @click="handleLogin">登录</button>
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
</style>
