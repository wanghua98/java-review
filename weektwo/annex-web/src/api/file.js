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

/** 获取当前用户目录下的文件夹和文件（分页） */
export function getUserDirList(page = 1, size = 20) {
  return get(`/api/file/dir/list?page=${page}&size=${size}`)
}

/** 查看指定目录下的子目录和文件（分页） */
export function getDirList(dirId, page = 1, size = 20) {
  return get(`/api/file/dir/list/${dirId}?page=${page}&size=${size}`)
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

/** 移动目录到其他目录 */
export function moveDir(dirId, targetDirId) {
  return postForm('/api/file/dir/move/' + dirId, { targetDirId })
}

/** 删除文件（软删除） */
export function deleteFile(fileId) {
  return postForm('/api/file/delete/' + fileId, {})
}

/** 删除目录（递归删除其下所有内容） */
export function deleteDir(dirId) {
  return postForm('/api/file/dir/delete/' + dirId, {})
}

/** 重命名文件 */
export function renameFile(fileId, newName) {
  return postForm('/api/file/rename/' + fileId, { newName })
}

/** 重命名目录 */
export function renameDir(dirId, newName) {
  return postForm('/api/file/dir/rename/' + dirId, { newName })
}

/** 获取文件下载链接 */
export function getDownloadUrl(fileId) {
  return '/api/file/download/' + fileId
}

/** 获取当前用户文件的短期 kkFileView 预览地址。 */
export function createPreviewTicket(fileId) {
  return postJson('/api/file/preview-ticket/' + fileId, {})
}

/** 创建文件分享，默认有效24小时。 */
export function createFileShare(fileId, expiresInHours = 24) {
  return postJson('/api/file/shares', {fileId, expiresInHours})
}

/** 查看当前用户创建过的分享。 */
export function listFileShares() {
  return get('/api/file/shares')
}

/** 撤销分享。 */
export function revokeFileShare(shareId) {
  return postForm(`/api/file/shares/${shareId}/revoke`, {})
}

/** 获取公开分享信息。 */
export function getPublicShare(token) {
  return get(`/api/public/shares/${encodeURIComponent(token)}`)
}

/** 获取公开分享的短期预览地址。 */
export function createPublicSharePreviewTicket(token) {
  return get(`/api/public/shares/${encodeURIComponent(token)}/preview-ticket`)
}

/** 获取公开分享下载地址。 */
export function getPublicShareDownloadUrl(token) {
  return `/api/public/shares/${encodeURIComponent(token)}/download`
}
