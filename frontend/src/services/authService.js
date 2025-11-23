import api from './apiService'

export const authService = {
    async login(account, password) {
        try {
            // 后端使用 session，返回格式 { success, message, data }
            const response = await api.post('/users/login', null, {
                params: { account, password }
            })
            // 后端返回格式: { success: true, message: "...", data: {...} }
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '登录失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '登录失败')
        }
    },

    async register(userData) {
        try {
            // userData 应包含后端期望的字段：userPassword, phoneNumber, userEmail, userName, gender, schoolWorkunit
            const response = await api.post('/users/register', userData)
            // 后端返回格式: { success: true, message: "...", data: {...} }
            if (response.data.success) {
                return response.data.data || response.data
            }
            throw new Error(response.data.message || '注册失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '注册失败')
        }
    },

    async checkAuth() {
        try {
            const response = await api.get('/users/check')
            if (response.data.success && response.data.data?.isLoggedIn) {
                return response.data.data
            }
            return null
        } catch (error) {
            return null
        }
    },

    async getProfile() {
        try {
            const response = await api.get('/users/profile')
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取用户信息失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取用户信息失败')
        }
    },

    async logout() {
        try {
            await api.post('/users/logout')
        } catch (error) {
            console.error('注销失败:', error)
        }
    }
}
