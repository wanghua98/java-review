<script setup>
import {onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {
  createPublicSharePreviewTicket,
  getPublicShare,
  getPublicShareDownloadUrl,
} from '@/api/file.js'

const route = useRoute()
const token = String(route.params.token || '')
const share = ref(null)
const loading = ref(true)
const message = ref('')

onMounted(async () => {
  try {
    const res = await getPublicShare(token)
    if (res.code === 200 && res.data) share.value = res.data
    else message.value = res.message || '分享不存在或已过期'
  } catch (e) {
    message.value = '无法加载分享信息'
  } finally {
    loading.value = false
  }
})

async function preview() {
  const previewWindow = window.open('about:blank', '_blank')
  if (previewWindow) previewWindow.opener = null
  try {
    const res = await createPublicSharePreviewTicket(token)
    if (res.code === 200 && res.data?.previewUrl) {
      if (previewWindow) previewWindow.location.replace(res.data.previewUrl)
      else window.location.href = res.data.previewUrl
    } else {
      if (previewWindow) previewWindow.close()
      message.value = res.message || '无法预览此分享'
    }
  } catch (e) {
    if (previewWindow) previewWindow.close()
    message.value = '预览服务暂时不可用'
  }
}

function formatSize(bytes) {
  if (bytes == null) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 ** 2) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 ** 3) return `${(bytes / 1024 ** 2).toFixed(1)} MB`
  return `${(bytes / 1024 ** 3).toFixed(2)} GB`
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}
</script>

<template>
  <main class="share-page">
    <section class="share-card">
      <h2>文件分享</h2>
      <p v-if="loading" class="hint">正在加载...</p>
      <p v-else-if="message && !share" class="error">{{ message }}</p>
      <template v-else-if="share">
        <div class="file-icon">📄</div>
        <h3>{{ share.fileName }}</h3>
        <p class="hint">文件大小：{{ formatSize(share.fileSize) }}</p>
        <p class="hint">有效期至：{{ formatTime(share.expiresAt) }}</p>
        <p v-if="message" class="error">{{ message }}</p>
        <div class="buttons">
          <button @click="preview">在线预览</button>
          <a :href="getPublicShareDownloadUrl(token)">下载文件</a>
        </div>
      </template>
    </section>
  </main>
</template>

<style scoped>
.share-page { min-height: calc(100vh - 49px); display: grid; place-items: center; background: #f5f7fa; padding: 24px; box-sizing: border-box; }
.share-card { width: min(480px, 100%); padding: 32px; background: #fff; border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,.08); text-align: center; }
.share-card h2 { margin-top: 0; }
.file-icon { font-size: 54px; margin-top: 24px; }
.share-card h3 { overflow-wrap: anywhere; }
.hint { color: #777; font-size: 14px; }
.error { color: #d93025; font-size: 14px; }
.buttons { display: flex; justify-content: center; gap: 12px; margin-top: 24px; }
.buttons button, .buttons a { border: 0; border-radius: 6px; padding: 9px 18px; font-size: 14px; cursor: pointer; text-decoration: none; }
.buttons button { background: #409eff; color: #fff; }
.buttons a { background: #67c23a; color: #fff; }
</style>
