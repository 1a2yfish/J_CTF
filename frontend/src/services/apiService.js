import axios from 'axios'
import { showError } from '@/utils/message'

// 创建axios实例
const api = axios.create({
    baseURL: '/api',
    timeout: 30000, // 增加超时时间到30秒
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
api.interceptors.request.use(
    config => {
        // 添加认证token
        const token = localStorage.getItem('ctf_token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        
        // 添加请求时间戳（防止缓存）
        if (config.method === 'get') {
            config.params = {
                ...config.params,
                _t: Date.now()
            }
        }
        
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
api.interceptors.response.use(
    response => {
        // 统一处理响应数据格式
        const { data } = response
        
        // 如果后端返回的是标准格式 { success, message, data }
        if (data && typeof data === 'object' && 'success' in data) {
            if (!data.success && data.message) {
                // 业务错误，但不在这里显示，由调用方处理
                return Promise.reject(new Error(data.message))
            }
            return response
        }
        
        return response
    },
    error => {
        // 统一处理错误
        if (error.response) {
            const { status, data } = error.response
            
            // 401 未授权，跳转登录
            if (status === 401) {
                localStorage.removeItem('ctf_token')
                localStorage.removeItem('ctf_user')
                window.location.href = '/login'
                return Promise.reject(error)
            }
            
            // 403 权限不足
            if (status === 403) {
                showError(data?.message || '权限不足')
                return Promise.reject(error)
            }
            
            // 500 服务器错误
            if (status === 500) {
                showError(data?.message || '服务器错误，请稍后重试')
                return Promise.reject(error)
            }
            
            // 其他错误，返回错误信息供调用方处理
            return Promise.reject(error)
        }
        
        // 网络错误
        if (error.request) {
            showError('网络错误，请检查网络连接')
            return Promise.reject(error)
        }
        
        // 其他错误
        showError(error.message || '请求失败')
        return Promise.reject(error)
    }
)

export default api
