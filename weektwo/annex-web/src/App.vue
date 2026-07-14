<!--
  根组件
  使用 <router-view> 展示当前路由对应的页面。
  顶部导航栏提供页面切换链接。
-->
<script setup>
// 不需要额外逻辑，所有功能分散在各页面组件中
</script>

<template>
  <div id="app-root">
    <!-- 顶部导航栏 -->
    <nav>
      <router-link to="/">首页</router-link>
      <router-link to="/files">我的文件</router-link>
      <router-link to="/login">登录</router-link>
      <router-link to="/register">注册</router-link>
    </nav>

    <!-- 路由页面渲染位置 -->
    <router-view />
  </div>
</template>

<style scoped>
nav { display: flex; gap: 16px; padding: 12px 24px; border-bottom: 1px solid #eee; }
nav a { text-decoration: none; color: #333; font-size: 14px; }
nav a:hover { color: #409eff; }
</style>
