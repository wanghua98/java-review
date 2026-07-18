/**
 * 用户相关 API 请求模块
 *
 * 封装了登录、注册、登出、获取/修改用户信息的请求方法。
 * 所有请求自动携带 Cookie（Sa-Token 通过 Cookie 传递令牌）。
 */

/**
 * 通用请求方法
 * @param {string} path   - 接口路径（如 '/api/login'）
 * @param {object} options - fetch 配置项（method、body 等）
 * @returns {Promise<object>} 后端返回的 JSON 数据
 *
 * 使用相对路径，请求由 Vite Dev Server 代理转发到后端，
 * 避免跨域问题和局域网访问时地址不对的问题。
 */
async function request(path, options = {}) {
  // 默认配置：携带 Cookie、JSON 格式
  const config = {
    credentials: 'include',           // 跨域携带 Cookie（Sa-Token 依赖）
    headers: { 'Content-Type': 'application/json' },
    ...options,
  }
  // 发送请求（相对路径，由 Vite 代理转发）
  const res = await fetch(path, config)
  // 解析 JSON 响应
  return res.json()
}


/**
 * 登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<object>} 登录结果（token 在 Cookie 中）
 */
export function login(username, password) {
  return request('/api/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

/**
 * 注册
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<object>} 注册结果
 */
export function register(username, password) {
  return request('/api/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

/**
 * 登出
 * @returns {Promise<object>} 登出结果
 */
export function logout() {
  return request('/api/logout')
}

/**
 * 获取当前登录用户信息
 * @returns {Promise<object>} 用户信息（密码字段被隐藏）
 */
export function getUserInfo() {
  return request('/api/info')
}

/**
 * 修改用户信息
 * @param {object} data - 要修改的字段（如 { username, nickname }）
 * @returns {Promise<object>} 修改结果
 */
export function changeInfo(data) {
  return request('/api/changeInfo', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}
