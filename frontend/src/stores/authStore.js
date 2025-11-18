import { defineStore } from 'pinia'
import { authService } from '../services/authService'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('ctf_token') || null,
        user: null,
        isAuthenticated: false,
        loading: false,
        error: null
    }),

    actions: {
        async login(account, password) {
            this.loading = true
            this.error = null

            try {
                const { token, user } = await authService.login(account, password)
                this.token = token
                this.user = user
                this.isAuthenticated = true

                // 保存到localStorage
                localStorage.setItem('ctf_token', token)
                localStorage.setItem('ctf_user', JSON.stringify(user))
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async register(account, password, role) {
            this.loading = true
            this.error = null

            try {
                await authService.register(account, password, role)
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        logout() {
            this.token = null
            this.user = null
            this.isAuthenticated = false

            // 清除localStorage
            localStorage.removeItem('ctf_token')
            localStorage.removeItem('ctf_user')
        },

        checkAuth() {
            const token = localStorage.getItem('ctf_token')
            const user = localStorage.getItem('ctf_user')

            if (token && user) {
                this.token = token
                this.user = JSON.parse(user)
                this.isAuthenticated = true
            }
        }
    }
})
