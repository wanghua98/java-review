<!--
  文件管理页面
  展示用户目录下的文件夹和文件列表。
  - 面包屑导航显示当前路径，点击文件夹进入子目录
  - 新建文件夹、上传文件（分片上传）、移动文件、下载文件
-->
<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {createSHA256} from 'hash-wasm';
import {
  getUserDirList, getDirList, getDownloadUrl, createDir,
  initUpload, uploadChunk, moveFile,
  deleteFile, deleteDir,
} from '@/api/file.js'

const router = useRouter()

// 当前目录信息
const currentDir = ref(null)
const subDirs = ref([])
const files = ref([])
const dirHistory = ref([])       // 面包屑历史
const loading = ref(true)
const message = ref('')

// 新建文件夹
const showNewDirInput = ref(false)
const newDirName = ref('')

// 上传文件
const uploadMsg = ref('')        // 上传状态提示
const uploading = ref(false)     // 是否正在上传
const uploadProgress = ref(0)    // 上传进度（已传分片数 / 总分片数）

/** 分片大小：5MB */
const CHUNK_SIZE = 5 * 1024 * 1024

/** 是否尝试秒传（勾选时计算 SHA-256，后端根据哈希判断是否已存在） */
const tryInstantUpload = ref(true)

/** 暂停上传标志 */
const paused = ref(false)

/** 续传时已上传的分片编号集合（用于跳过已完成分片） */
const uploadedChunkSet = ref(new Set())

/** 当前正在上传的文件对象（暂停后用于继续上传） */
const currentFile = ref(null)

/** 缓存已计算的 SHA-256（暂停后续传时复用，避免重复计算） */
const cachedHashHex = ref('')

// 移动文件（逐层目录选择）
const moveFileId = ref(null)     // 正在移动的文件ID
const moveDir = ref(null)        // 当前浏览的目标目录
const moveSubDirs = ref([])      // 当前目录下的子目录列表
const moveDirHistory = ref([])   // 目录导航历史

// 删除确认
const deleteTarget = ref(null)   // { type:'file'|'dir', item }

/**
 * 加载指定目录的内容
 */
async function loadDir(dirId) {
  loading.value = true
  message.value = ''
  try {
    const res = dirId ? await getDirList(dirId) : await getUserDirList()
    if (res.code === 200 && res.data) {
      currentDir.value = res.data.currentDir
      subDirs.value = res.data.subDirs || []
      files.value = res.data.files || []
    } else {
      if (res.message === '用户未登录') router.push('/login')
      else message.value = res.message || '加载失败'
    }
  } catch (e) {
    message.value = '网络错误'
  } finally {
    loading.value = false
  }
}

function enterDir(dir) {
  if (currentDir.value) dirHistory.value.push(currentDir.value)
  loadDir(dir.id)
}

function goBack() {
  if (dirHistory.value.length === 0) return
  currentDir.value = dirHistory.value.pop()
  loadDir(currentDir.value.id)
}

/** 新建文件夹 */
async function handleCreateDir() {
  if (!newDirName.value.trim()) {
    message.value = '请输入目录名称';
    return
  }
  if (!currentDir.value) return
  const res = await createDir(currentDir.value.id, newDirName.value.trim())
  if (res.code === 200) {
    showNewDirInput.value = false
    newDirName.value = ''
    message.value = ''
    loadDir(currentDir.value.id)
  } else {
    message.value = res.message || '创建失败'
  }
}

/**
 * 上传文件（分片上传）
 * 流程：计算 SHA-256 → init 上传任务 → 逐片 upload
 * 文件 < 5MB → 1 个分片，文件 >= 5MB → 按 5MB 切分
 */
async function handleUpload(event) {
  const file = event.target.files[0]
  if (!file || !currentDir.value) return

  // 限制文件大小 10GB
  if (file.size > 10 * 1024 * 1024 * 1024) {
    uploadMsg.value = '文件超过 10GB 限制'
    event.target.value = ''
    return
  }

  currentFile.value = file

  // 首次上传时计算 SHA-256（暂停后续传复用缓存值，不重复计算）
  if (tryInstantUpload.value && file.size <= 10000 * 1024 * 1024) {
    uploadMsg.value = '正在计算文件哈希...'
    cachedHashHex.value = await computeSha256Chunked(file)
  }

  await doUpload(file, cachedHashHex.value)
  // 上传完成（非暂停）后清理
  if (!paused.value) {
    currentFile.value = null
    cachedHashHex.value = ''
  }
  event.target.value = ''
}

/** 暂停后继续上传（复用已缓存的 SHA-256，不重新计算） */
async function resumeUpload() {
  if (!currentFile.value) return
  paused.value = false
  await doUpload(currentFile.value, cachedHashHex.value)
  if (!paused.value) {
    currentFile.value = null
    cachedHashHex.value = ''
  }
}

/**
 * 核心上传逻辑：初始化任务 → 逐片上传
 * @param {File} file - 要上传的文件对象
 * @param {string} hashHex - SHA-256 哈希（由调用方计算并传入）
 */
async function doUpload(file, hashHex) {
  uploading.value = true
  paused.value = false
  uploadedChunkSet.value = new Set()
  uploadMsg.value = '准备上传: ' + file.name
  uploadProgress.value = 0

  try {
    // 1. 计算分片数（小于 CHUNK_SIZE 则 = 1）
    const chunkCount = Math.max(1, Math.ceil(file.size / CHUNK_SIZE))

    // 2. 初始化上传任务（后端自动识别续传：同一用户 + 同一 SHA-256 + 上传中的任务）
    const initRes = await initUpload({
      fileName: file.name,
      fileSize: file.size,
      fileSha256: hashHex,
      parentId: currentDir.value.id,
      chunkCount: chunkCount,
    })
    if (initRes.code !== 200) {
      uploadMsg.value = '上传失败: ' + (initRes.message || '初始化失败')
      return
    }
    if (initRes.data.id === -1) {
      uploadMsg.value = '秒传成功'
      loadDir(currentDir.value.id)
      return
    }
    const taskId = initRes.data.id

    // 续传判断：uploadedCount > 0 表示已有分片上传过
    if (initRes.data.uploadedCount > 0 && initRes.data.uploadedChunks) {
      uploadedChunkSet.value = new Set(initRes.data.uploadedChunks)
      uploadMsg.value = '续传: ' + file.name + ' (已上传 ' + uploadedChunkSet.value.size + '/' + chunkCount + ' 个分片)'
    } else {
      uploadMsg.value = '正在上传: ' + file.name + ' (' + chunkCount + ' 个分片)'
    }

    // 3. 逐分片上传
    for (let i = 1; i <= chunkCount; i++) {
      // 暂停检查
      if (paused.value) {
        uploadMsg.value = '已暂停: ' + file.name + ' (已传 ' + (i - 1) + '/' + chunkCount + ' 个分片)'
        return
      }
      // 跳过已上传的分片（续传场景）
      if (uploadedChunkSet.value.has(i)) {
        uploadProgress.value = i
        continue
      }
      uploadMsg.value = '上传中: ' + file.name + ' (' + i + '/' + chunkCount + ')'
      // 切片：取该分片的数据块
      const start = (i - 1) * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, file.size)
      const chunk = file.slice(start, end)

      const chunkRes = await uploadChunk(taskId, i, chunk)
      if (chunkRes.code !== 200) {
        uploadMsg.value = '上传失败: ' + (chunkRes.message || '分片 ' + i + ' 错误')
        return
      }
      uploadProgress.value = i
    }

    uploadMsg.value = file.name + ' 上传成功，后台正在处理你的文件请稍后查看'
    uploadedChunkSet.value = new Set()
    loadDir(currentDir.value.id)
  } catch (e) {
    uploadMsg.value = '上传出错: ' + e.message
  } finally {
    uploading.value = false
  }
}

/**
 * 分片计算 SHA-256
 * @param {File} file          - 文件对象
 * @param {number} chunkSize   - 每块大小（字节），默认 8MB
 * @returns {Promise<string>}  - 十六进制哈希
 */
async function computeSha256Chunked(file, chunkSize = 8 * 1024 * 1024) {
  const start = Date.now()
  // 初始化增量哈希器
  const hasher = await createSHA256();
  hasher.init();

  let offset = 0;
  const total = file.size;

  while (offset < total) {
    // 每次只切出 chunkSize 大小的块（最后一块可能不足）
    const chunk = file.slice(offset, offset + chunkSize);
    const buffer = await chunk.arrayBuffer();
    hasher.update(new Uint8Array(buffer));

    offset += chunkSize;

    // 可选：输出进度（不影响速度，但便于观察）
    console.log(`进度: ${((offset / total) * 100).toFixed(1)}%`);

  }
  console.log(`耗时: ${Date.now() - start}ms`);
  return hasher.digest('hex');
}

/**
 * 打开移动选择器
 * 从用户个人目录开始，可逐层点击进入子目录选择目标位置
 */
async function showMovePicker(file) {
  moveFileId.value = file.id
  moveDirHistory.value = []
  // 加载用户个人目录作为起始目录
  const res = await getUserDirList()
  if (res.code === 200 && res.data) {
    moveDir.value = res.data.currentDir
    moveSubDirs.value = res.data.subDirs || []
  } else {
    moveDir.value = null
    moveSubDirs.value = []
  }
}

/** 进入子目录（移动弹窗内） */
async function enterMoveDir(dir) {
  if (moveDir.value) moveDirHistory.value.push(moveDir.value)
  const res = await getDirList(dir.id)
  if (res.code === 200 && res.data) {
    moveDir.value = res.data.currentDir
    moveSubDirs.value = res.data.subDirs || []
  }
}

/** 返回上级目录（移动弹窗内） */
async function goBackMoveDir() {
  if (moveDirHistory.value.length === 0) return
  const prev = moveDirHistory.value.pop()
  moveDir.value = prev
  // 重新加载上级目录的子目录列表
  const res = await getDirList(prev.id)
  if (res.code === 200 && res.data) {
    moveSubDirs.value = res.data.subDirs || []
  }
}

/**
 * 确认移动文件
 */
async function handleMove() {
  if (!moveFileId.value || !moveDir.value) return
  const res = await moveFile(moveFileId.value, moveDir.value.id)
  if (res.code === 200) {
    moveFileId.value = null
    moveDir.value = null
    moveSubDirs.value = []
    moveDirHistory.value = []
    message.value = ''
    loadDir(currentDir.value?.id)
  } else {
    message.value = '移动失败: ' + (res.message || '未知错误')
  }
}

function cancelMove() {
  moveFileId.value = null
  moveDir.value = null
  moveSubDirs.value = []
  moveDirHistory.value = []
}

/** 打开文件删除确认 */
function confirmDeleteFile(file) {
  deleteTarget.value = {type: 'file', item: file}
}

/** 打开目录删除确认 */
function confirmDeleteDir(dir) {
  deleteTarget.value = {type: 'dir', item: dir}
}

/** 执行删除 */
async function handleDelete() {
  if (!deleteTarget.value) return
  const target = deleteTarget.value
  try {
    let res
    if (target.type === 'file') {
      res = await deleteFile(target.item.id)
    } else {
      res = await deleteDir(target.item.id)
    }
    if (res.code === 200) {
      deleteTarget.value = null
      message.value = ''
      loadDir(currentDir.value?.id)
    } else {
      message.value = '删除失败: ' + (res.message || '未知错误')
      deleteTarget.value = null
    }
  } catch (e) {
    message.value = '删除失败: ' + e.message
    deleteTarget.value = null
  }
}

function cancelDelete() {
  deleteTarget.value = null
}

/**
 * 格式化文件大小，根据大小自动选择合适单位
 * - < 1 KB   → 显示 B
 * - < 1 MB   → 显示 KB
 * - < 1 GB   → 显示 MB
 * - >= 1 GB  → 显示 GB
 */
function formatFileSize(bytes) {
  if (bytes == null || bytes < 0) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

onMounted(() => {
  loadDir(null)
})
</script>

<template>
  <div class="page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <h2>我的文件</h2>
      <div class="actions">
        <button class="btn" @click="showNewDirInput = !showNewDirInput">+ 新建文件夹</button>
        <label class="btn upload-btn" :class="{ disabled: uploading }">
          {{ uploading ? '上传中...' : '上传文件' }}
          <input type="file" hidden :disabled="uploading" @change="handleUpload"/>
        </label>
        <label class="instant-check" title="勾选后计算文件 SHA-256 哈希，后端可判断是否秒传">
          <input type="checkbox" v-model="tryInstantUpload" :disabled="uploading"/>
          尝试秒传
        </label>
        <button v-if="uploading && !paused" class="btn pause" @click="paused = true">暂停</button>
        <button v-if="paused" class="btn resume" @click="resumeUpload">继续上传</button>
      </div>
    </div>

    <!-- 上传状态 -->
    <p v-if="uploadMsg" class="msg">{{ uploadMsg }}</p>

    <!-- 新建文件夹 -->
    <div class="new-dir" v-if="showNewDirInput">
      <input v-model="newDirName" placeholder="输入文件夹名称" @keyup.enter="handleCreateDir"/>
      <button class="btn" @click="handleCreateDir">确定</button>
      <button class="btn cancel" @click="showNewDirInput = false; newDirName = ''">取消</button>
    </div>

    <!-- 返回 -->
    <div class="breadcrumb">
      <span class="link" @click="goBack" v-if="dirHistory.length > 0">← 返回</span>
      <span class="sep" v-if="dirHistory.length > 0"> / </span>
      <span>{{ currentDir?.name || '加载中...' }}</span>
    </div>

    <!-- 加载中 -->
    <p v-if="loading" class="msg">加载中...</p>

    <!-- 提示 -->
    <p v-else-if="message" class="msg error">{{ message }}</p>

    <!-- 文件列表 -->
    <div v-else class="list">
      <div v-if="subDirs.length === 0 && files.length === 0" class="empty">此目录为空</div>

      <div v-for="dir in subDirs" :key="dir.id" class="item dir" @click="enterDir(dir)">
        <span class="icon">📁</span>
        <span class="name">{{ dir.name }}</span>
        <span class="action-link del" @click.stop="confirmDeleteDir(dir)">删除</span>
      </div>

      <div v-for="file in files" :key="file.id" class="item file">
        <span class="icon">📄</span>
        <span class="name">{{ file.fileName }}</span>
        <span class="size">{{ formatFileSize(file.fileSize) }}</span>
        <a class="action-link" :href="getDownloadUrl(file.id)" target="_blank">下载</a>
        <span class="action-link move" @click="showMovePicker(file)">移动</span>
        <span class="action-link del" @click="confirmDeleteFile(file)">删除</span>
      </div>
    </div>

    <!-- 移动弹窗（逐层目录导航） -->
    <div class="move-modal" v-if="moveFileId">
      <div class="move-box">
        <p>
          <strong>
            <span v-if="moveDirHistory.length > 0" class="link" @click="goBackMoveDir">← 返回上级</span>
            <span v-if="moveDirHistory.length > 0"> / </span>
            {{ moveDir?.name || '加载中...' }}
          </strong>
        </p>
        <div class="move-dirs">
          <div v-for="dir in moveSubDirs" :key="dir.id"
               class="move-dir"
               @click="enterMoveDir(dir)">
            📁 {{ dir.name }}
          </div>
          <div v-if="moveSubDirs.length === 0" class="move-dir disabled">此目录下没有子目录</div>
        </div>
        <div class="move-actions">
          <button class="btn" @click="handleMove" :disabled="!moveDir">移动到此处</button>
          <button class="btn cancel" @click="cancelMove">取消</button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="move-modal" v-if="deleteTarget">
      <div class="move-box">
        <p><strong>确定要删除「{{ deleteTarget.item.name || deleteTarget.item.fileName }}」吗？</strong></p>
        <p class="del-hint">删除后数据将不可见，如需恢复可联系管理员</p>
        <div class="move-actions">
          <button class="btn del-confirm" @click="handleDelete">确定删除</button>
          <button class="btn cancel" @click="cancelDelete">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 700px;
  margin: 20px auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar h2 {
  margin: 0;
  font-size: 18px;
}

.instant-check {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn.pause {
  background: #e6a23c;
  color: #fff;
}

.btn.resume {
  background: #67c23a;
  color: #fff;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.breadcrumb {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

.breadcrumb .link {
  color: #409eff;
  cursor: pointer;
}

.breadcrumb .link:hover {
  text-decoration: underline;
}

.breadcrumb .sep {
  margin: 0 4px;
}

.list {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.item:last-child {
  border-bottom: none;
}

.item.dir {
  cursor: pointer;
}

.item.dir:hover {
  background: #f5f7fa;
}

.icon {
  margin-right: 10px;
  font-size: 16px;
}

.name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.size {
  margin-right: 16px;
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}

.action-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
  cursor: pointer;
  margin-left: 12px;
}

.action-link:hover {
  text-decoration: underline;
}

.action-link.move {
  color: #67c23a;
}

.action-link.del {
  color: #f56c6c;
}

.empty {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.msg {
  text-align: center;
  color: #999;
  padding: 20px;
  font-size: 13px;
}

.msg.error {
  color: red;
}

.new-dir {
  display: flex;
  gap: 8px;
  margin: 10px 0;
  align-items: center;
}

.new-dir input {
  flex: 1;
  padding: 6px 10px;
  font-size: 14px;
}

.btn {
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
}

.btn:hover {
  background: #f5f5f5;
}

.btn.cancel {
  color: #999;
}

.btn:disabled, .btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.upload-btn {
  cursor: pointer;
  color: #409eff;
  border-color: #409eff;
}

/* 移动弹窗 */
.move-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.move-box {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  min-width: 320px;
  max-width: 500px;
}

.move-box p {
  margin: 0 0 12px;
  font-size: 15px;
}

.move-dirs {
  max-height: 260px;
  overflow-y: auto;
  margin-bottom: 12px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.move-dir {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
}

.move-dir:hover {
  background: #f5f7fa;
}

.move-dir.selected {
  background: #ecf5ff;
  color: #409eff;
  font-weight: bold;
}

.move-dir.disabled {
  color: #ccc;
  cursor: default;
}

.move-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.del-hint {
  font-size: 13px;
  color: #999;
  margin: 6px 0 14px !important;
}

.del-confirm {
  color: #fff;
  background: #f56c6c;
  border-color: #f56c6c;
}

.del-confirm:hover {
  background: #e04040;
  border-color: #e04040;
}
</style>
