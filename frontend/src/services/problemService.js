import api from './apiService'

export const problemService = {
    async getChallenges(competitionId = null, page = 0, size = 20, category = null, difficulty = null, keyword = null) {
        try {
            const params = { page, size }
            // 确保 competitionId 是有效的数字
            if (competitionId && competitionId !== 'undefined' && competitionId !== 'null') {
                const numId = parseInt(competitionId, 10)
                if (!isNaN(numId)) {
                    params.competitionId = numId
                }
            }
            // 确保筛选参数正确传递（即使为空字符串也不传递）
            if (category && category.trim() !== '') {
                params.category = category.trim()
            }
            if (difficulty && difficulty.trim() !== '') {
                params.difficulty = difficulty.trim()
            }
            if (keyword && keyword.trim() !== '') {
                params.keyword = keyword.trim()
            }

            console.log('题目API请求参数:', params)
            const response = await api.get('/challenges', { params })
            console.log('题目API响应:', response.data)
            if (response.data.success && response.data.data) {
                const data = response.data.data
                // 确保返回的数据结构正确
                if (data.challenges && Array.isArray(data.challenges)) {
                    return data
                } else if (Array.isArray(data)) {
                    // 如果直接返回数组，包装成对象
                    return { challenges: data, totalPages: 1, totalElements: data.length, currentPage: 0 }
                }
                return { challenges: [], totalPages: 0, totalElements: 0, currentPage: 0 }
            }
            return { challenges: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            console.error('获取题目列表错误:', error)
            throw new Error(error.response?.data?.message || error.message || '获取题目列表失败')
        }
    },

    async getChallengeById(challengeId) {
        try {
            const response = await api.get(`/challenges/${challengeId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取题目详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取题目详情失败')
        }
    },

    async getProblemsByCompetition(competitionId) {
        try {
            const data = await this.getChallenges(competitionId, 0, 100)
            return data.challenges || []
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取题目失败')
        }
    },

    async submitFlag(challengeId, flag) {
        try {
            const response = await api.post(`/challenges/${challengeId}/submit`, { flag })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '提交 Flag 失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '提交 Flag 失败')
        }
    },

    async checkSolved(challengeId) {
        try {
            const response = await api.get(`/challenges/${challengeId}/solved`)
            if (response.data.success && response.data.data) {
                return response.data.data.solved || false
            }
            return false
        } catch (error) {
            return false
        }
    },

    async getHints(challengeId) {
        try {
            const response = await api.get(`/challenges/${challengeId}/hints`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return []
        } catch (error) {
            return []
        }
    }
}
