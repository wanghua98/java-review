/**
 * 文件管理 API 请求模块
 *
 * 封装了目录列表、新建文件夹、上传（分片）、移动、下载等接口。
 * 所有请求走 Vite 代理（/api → 后端），无需写完整地址。
 */

/**
 * 通用 GET 请求
 */
async function get(path) {
  const res = await fetch(path, { credentials: 'include' })
  return res.json()
}

/**
 * 通用 POST 请求（表单格式，如 ?key=value）
 */
async function postForm(path, params) {
  const res = await fetch(path, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(params),
  })
  return res.json()
}

/**
 * POST JSON 请求（后端 @RequestBody 接收）
 */
async function postJson(path, data) {
  const res = await fetch(path, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return res.json()
}

/**
 * POST 上传文件（multipart/form-data）
 */
async function postFile(path, data) {
  const res = await fetch(path, {
    method: 'POST',
    credentials: 'include',
    body: data,
  })
  return res.json()
}

// ==================== 导出方法 ====================

/** 获取当前用户目录下的文件夹和文件 */
export function getUserDirList() {
  return get('/api/file/dir/list')
}

/** 查看指定目录下的子目录和文件 */
export function getDirList(dirId) {
  return get('/api/file/dir/list/' + dirId)
}

/** 获取当前用户的所有目录（平铺列表，用于移动文件） */
export function getAllDirs() {
  return get('/api/file/dir/all')
}

/** 在指定目录下新建子目录 */
export function createDir(parentId, name) {
  return postForm('/api/file/dir/create', { parentId, name })
}

/**
 * 初始化上传任务（分片上传第一步）
 * 后端 @RequestBody 接收 JSON，所以用 postJson
 */
export function initUpload({ fileName, fileSize, fileSha256, parentId, chunkCount }) {
  const suffix = fileName.includes('.') ? fileName.split('.').pop() : ''
  return postJson('/api/file/init', {
    fileName, fileSize, fileSha256, parentId, chunkCount, fileSuffix: suffix,
  })
}

/**
 * 上传单个分片（分片上传第二步）
 * @param {number|string} taskId      - 上传任务ID
 * @param {number}        chunkNumber - 分片编号（从1开始）
 * @param {Blob}          chunkData   - 分片数据
 */
export function uploadChunk(taskId, chunkNumber, chunkData) {
  const form = new FormData()
  form.append('taskId', taskId)
  form.append('chunkNumber', chunkNumber)
  form.append('file', chunkData)
  return postFile('/api/file/upload', form)
}

/** 移动文件到其他目录 */
export function moveFile(fileId, targetDirId) {
  return postForm('/api/file/move', { fileId, targetDirId })
}

/** 删除文件（软删除） */
export function deleteFile(fileId) {
  return postForm('/api/file/delete/' + fileId, {})
}

/** 删除目录（递归删除其下所有内容） */
export function deleteDir(dirId) {
  return postForm('/api/file/dir/delete/' + dirId, {})
}

/** 获取文件下载链接 */
export function getDownloadUrl(fileId) {
  return '/api/file/download/' + fileId
}

/**
 * 获取 kkFileView 在线预览 URL
 * @param {number} fileId - 文件ID
 * @param {string} suffix  - 文件后缀（如 'jpg', 'pdf'）
 * @returns {string}        - 完整的 kkFileView 预览地址
 */
export function getPreviewUrl(fileId, suffix) {
  // 统一使用 localhost（需要在容器内把 localhost:8001 转发到宿主机）
  const fileUrl = `http://localhost:8001/api/file/inline/${fileId}.${suffix}`
  // 标准 base64 编码并 URL 编码
  const base64 = btoa(fileUrl)
  return `http://localhost:8012/onlinePreview?url=${encodeURIComponent(base64)}`
}
