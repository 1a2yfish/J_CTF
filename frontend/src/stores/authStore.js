import { defineStore } from 'pinia'
import { authService } from '../services/authService'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('ctf_token') || null,
        user: JSON.parse(localStorage.getItem('ctf_user') || 'null'),
        isAuthenticated: !!localStorage.getItem('ctf_user'),
        loading: false,
        error: null
    }),

    getters: {
        isAdmin: (state) => {
            return state.user?.userRole === 'ADMIN' || state.user?.userType === 'ADMIN'
        }
    },

    actions: {
        async login(account, password) {
            this.loading = true
            this.error = null

            try {
                // authService.login 已经返回了 data 字段的内容
                const user = await authService.login(account, password)

                this.token = null
                this.user = user
                this.isAuthenticated = !!user

                // 保存到localStorage
                localStorage.removeItem('ctf_token')
                localStorage.setItem('ctf_user', JSON.stringify(user))
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async register(userData) {
            this.loading = true
            this.error = null

            try {
                await authService.register(userData)
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async logout() {
            try {
                await authService.logout()
            } catch (error) {
                console.error('注销请求失败:', error)
            } finally {
                this.token = null
                this.user = null
                this.isAuthenticated = false

                // 清除localStorage
                localStorage.removeItem('ctf_token')
                localStorage.removeItem('ctf_user')
            }
        },

        checkAuth() {
            const user = localStorage.getItem('ctf_user')

            if (user) {
                this.user = JSON.parse(user)
                this.isAuthenticated = true
            }
        }
    }
})
