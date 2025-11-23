import api from './apiService'

export const adminService = {
    // 系统统计
    async getDashboard() {
        try {
            const response = await api.get('/admin/dashboard')
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取仪表盘数据失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取仪表盘数据失败')
        }
    },

    // 用户管理
    async getUsers(page = 0, size = 20, sort = 'createTime', keyword = null) {
        try {
            const params = { page, size, sort }
            if (keyword) params.keyword = keyword
            const response = await api.get('/admin/users', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { users: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取用户列表失败')
        }
    },

    async getUserDetails(userId) {
        try {
            const response = await api.get(`/admin/users/${userId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取用户详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取用户详情失败')
        }
    },

    async updateUser(userId, userData) {
        try {
            const response = await api.put(`/admin/users/${userId}`, userData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '更新用户失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '更新用户失败')
        }
    },

    async disableUser(userId) {
        try {
            const response = await api.post(`/admin/users/${userId}/disable`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '禁用用户失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '禁用用户失败')
        }
    },

    async enableUser(userId) {
        try {
            const response = await api.post(`/admin/users/${userId}/enable`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '启用用户失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '启用用户失败')
        }
    },

    async deleteUser(userId) {
        try {
            const response = await api.delete(`/admin/users/${userId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '删除用户失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '删除用户失败')
        }
    },

    // 战队管理
    async getTeams(page = 0, size = 20, competitionId = null, auditState = null) {
        try {
            const params = { page, size }
            if (competitionId) params.competitionId = competitionId
            if (auditState) params.auditState = auditState
            const response = await api.get('/admin/teams', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { teams: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取战队列表失败')
        }
    },

    async getTeamDetails(teamId) {
        try {
            const response = await api.get(`/admin/teams/${teamId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取战队详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取战队详情失败')
        }
    },

    async auditTeam(teamId, auditState, auditRemark = null) {
        try {
            const response = await api.post(`/admin/teams/${teamId}/audit`, {
                auditState,
                auditRemark
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '审核战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '审核战队失败')
        }
    },

    async deleteTeam(teamId) {
        try {
            const response = await api.delete(`/admin/teams/${teamId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '删除战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '删除战队失败')
        }
    },

    async getTeamStatistics(competitionId) {
        try {
            const response = await api.get(`/admin/competitions/${competitionId}/team-stats`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return {}
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取战队统计失败')
        }
    },

    // 竞赛管理
    async getCompetitions(page = 0, size = 20, status = null) {
        try {
            const params = { page, size }
            if (status) params.status = status
            const response = await api.get('/admin/competitions', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { competitions: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取竞赛列表失败')
        }
    },

    async getCompetitionDetails(competitionId) {
        try {
            const response = await api.get(`/admin/competitions/${competitionId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取竞赛详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取竞赛详情失败')
        }
    },

    async createCompetition(competitionData) {
        try {
            const response = await api.post('/admin/competitions', competitionData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '创建竞赛失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '创建竞赛失败')
        }
    },

    async updateCompetition(competitionId, competitionData) {
        try {
            const response = await api.put(`/admin/competitions/${competitionId}`, competitionData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '更新竞赛失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '更新竞赛失败')
        }
    },

    async auditCompetition(competitionId, approved, auditRemark = null) {
        try {
            const response = await api.post(`/admin/competitions/${competitionId}/audit`, {
                approved,
                auditRemark
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '审核竞赛失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '审核竞赛失败')
        }
    },

    async deleteCompetition(competitionId) {
        try {
            const response = await api.delete(`/admin/competitions/${competitionId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '删除竞赛失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '删除竞赛失败')
        }
    },

    async getCompetitionStatistics(competitionId) {
        try {
            const response = await api.get(`/admin/competitions/${competitionId}/stats`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return {}
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取竞赛统计失败')
        }
    },

    // 题目管理
    async getChallenges(params = {}) {
        try {
            const response = await api.get('/challenges', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { challenges: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取题目列表失败')
        }
    },

    async getChallengeDetails(challengeId) {
        try {
            const response = await api.get(`/challenges/${challengeId}`)
            if (response.data.success && response.data.data) {
                // 如果返回的是 { challenge, solved, isAdmin } 格式，提取challenge
                if (response.data.data.challenge) {
                    return response.data.data.challenge
                }
                return response.data.data
            }
            throw new Error(response.data.message || '获取题目详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取题目详情失败')
        }
    },

    async createChallenge(challengeData) {
        try {
            const response = await api.post('/challenges', challengeData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '创建题目失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '创建题目失败')
        }
    },

    async updateChallenge(challengeId, challengeData) {
        try {
            const response = await api.put(`/challenges/${challengeId}`, challengeData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '更新题目失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '更新题目失败')
        }
    },

    async deleteChallenge(challengeId) {
        try {
            const response = await api.delete(`/challenges/${challengeId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '删除题目失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '删除题目失败')
        }
    },

    // Flag提交管理
    async getFlagSubmissions(params = {}) {
        try {
            const response = await api.get('/flags/submissions', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { submissions: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取Flag提交列表失败')
        }
    }
}

