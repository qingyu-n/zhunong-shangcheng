/**
 * Axios 请求封装
 * 包含请求拦截器、响应拦截器、错误处理
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码是 200，说明请求成功
    if (res.code === 200) {
      return res.data
    }
    
    // 其他状态码处理
    const errorMsg = res.msg || res.message || '请求失败'
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  },
  (error) => {
    const { response } = error
    
    if (response) {
      switch (response.status) {
        case 401:
          // Token 过期或无效
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.msg || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

// 封装 GET 请求
export const get = (url, params = {}) => {
  return request.get(url, { params })
}

// 封装 POST 请求
export const post = (url, data = {}) => {
  return request.post(url, data)
}

// 封装 PUT 请求
export const put = (url, data = {}) => {
  return request.put(url, data)
}

// 封装 DELETE 请求
export const del = (url, params = {}) => {
  return request.delete(url, { params })
}

export default request
