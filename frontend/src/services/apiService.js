import axios from 'axios'

// 创建axios实例
const api = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(config => {
    const token = localStorage.getItem('ctf_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 响应拦截器
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            // 处理未授权错误
            localStorage.removeItem('ctf_token')
            localStorage.removeItem('ctf_user')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default api
