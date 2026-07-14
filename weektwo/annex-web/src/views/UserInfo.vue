<!--
  用户信息主页
  展示当前登录用户的用户名、昵称等信息，并提供修改昵称功能。
  未登录时自动跳转到登录页。
  包含登出按钮。
-->
<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {getUserInfo, changeInfo, logout} from '@/api/user.js'

const router = useRouter()

// 用户数据（从后端获取）
const user = ref(null)
// 修改昵称表单
const newNickname = ref('')
// 修改密码表单
const newPassword = ref('')
const confirmPassword = ref('')
// 提示消息
const message = ref('')

/**
 * 页面加载时获取用户信息
 * 如果未登录，后端会返回错误，跳转到登录页
 */
onMounted(async () => {
  const res = await getUserInfo()
  if (res.code === 200 && res.data) {
    user.value = res.data
    newNickname.value = res.data.nickname || ''
  } else {
    // 未登录或登录过期，跳转登录页
    router.push('/login')
  }
})

/**
 * 修改昵称
 */
async function handleChangeNickname() {
  if (!newNickname.value.trim()) {
    message.value = '昵称不能为空'
    return
  }
  // 调用修改信息接口
  const res = await changeInfo({nickname: newNickname.value.trim()})
  if (res.code === 200) {
    message.value = '修改成功'
    // 更新本地显示
    if (user.value) user.value.nickname = newNickname.value.trim()
  } else {
    message.value = res.message || '修改失败'
  }
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  // 校验两次密码输入一致
  if (!newPassword.value) {
    message.value = '请输入新密码'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.value = '两次密码输入不一致'
    return
  }
  // 调用修改信息接口（传 password 字段）
  const res = await changeInfo({password: newPassword.value})
  if (res.code === 200) {
    message.value = '密码修改成功'
    // 清空输入框
    newPassword.value = ''
    confirmPassword.value = ''
  } else {
    message.value = res.message || '修改失败'
  }
}

/**
 * 登出
 * 调用后端登出接口后跳转到登录页
 */
async function handleLogout() {
  const res = await logout()
  if (res.code === 200) {
    router.push('/login')
  }
}
</script>

<template>
  <div class="page">
    <h2>用户中心</h2>

    <!-- 用户信息卡片 -->
    <div v-if="user" class="card">
      <p><strong>用户名：</strong>{{ user.username }}</p>
      <p><strong>昵　称：</strong>{{ user.nickname }}</p>
      <p><strong>角　色：</strong>{{ user.role }}</p>
    </div>

    <!-- 修改昵称 -->
    <div class="form">
      <h3>修改昵称</h3>
      <input v-model="newNickname" placeholder="输入新昵称"/>
      <button @click="handleChangeNickname">保存</button>
    </div>

    <!-- 修改密码 -->
    <div class="form">
      <h3>修改密码</h3>
      <input v-model="newPassword" type="password" placeholder="输入新密码"/>
      <input v-model="confirmPassword" type="password" placeholder="再次输入新密码"/>
      <button @click="handleChangePassword">修改密码</button>
    </div>

    <!-- 提示消息 -->
    <p v-if="message" class="msg">{{ message }}</p>

    <!-- 登出按钮 -->
    <button class="logout" @click="handleLogout">退出登录</button>
  </div>
</template>

<style scoped>
.page {
  max-width: 400px;
  margin: 40px auto;
  text-align: center;
}

.card {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
  text-align: left;
  margin-bottom: 20px;
}

.card p {
  margin: 6px 0;
  font-size: 14px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.form h3 {
  margin: 0;
  font-size: 15px;
}

input {
  padding: 8px 12px;
  font-size: 14px;
}

button {
  padding: 8px;
  cursor: pointer;
}

.logout {
  margin-top: 20px;
  background: #ff4d4f;
  color: #fff;
  border: none;
  padding: 8px 24px;
  border-radius: 4px;
  cursor: pointer;
}

.msg {
  color: green;
  font-size: 13px;
}
</style>
