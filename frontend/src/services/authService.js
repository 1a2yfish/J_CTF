import api from './apiService'

export const authService = {
    async login(account, password) {
        try {
            const response = await api.post('/auth/login', null, {
                params: {
                    account,
                    password
                }
            })
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '登录失败')
        }
    },

    async register(account, password, role) {
        try {
            const response = await api.post('/auth/register', {
                account,
                password,
                role
            })
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '注册失败')
        }
    }
}
