import axios from 'axios'
import { Message } from 'element-ui'
import Cookies from 'js-cookie'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    // 特殊处理blob类型的响应，用于文件下载
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    // 如果响应没有code字段（直接返回数据），直接通过
    if (res && res.code === undefined) {
      return res
    }
    if (res.code !== 200) {
      if (!response.config?.hideError) {
        Message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response?.status === 401) {
      // Token 过期，清除本地存储并跳转到登录页面
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
      Message.error('登录已过期，请重新登录')
      // 跳转到登录页面
      window.location.href = '/login'
    } else {
      // 检查是否需要显示错误消息
      if (!error.config?.hideError) {
        Message.error(error.response?.data?.message || '请求失败')
      }
    }
    return Promise.reject(error)
  }
)

export default request
