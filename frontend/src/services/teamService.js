import api from './apiService'

export const teamService = {
    async getMyTeam(competitionId) {
        try {
            // 确保 competitionId 是有效的数字
            const params = {}
            if (competitionId && competitionId !== 'undefined' && competitionId !== 'null') {
                const numId = parseInt(competitionId, 10)
                if (!isNaN(numId)) {
                    params.competitionId = numId
                }
            }
            const response = await api.get('/teams/my-team', { params })
            if (response.data.success && response.data.data) {
                // 如果返回hasTeam: false，说明没有团队
                if (response.data.data.hasTeam === false) {
                    return null
                }
                return response.data.data
            }
            return null
        } catch (error) {
            return null
        }
    },

    async getMyTeams(page = 0, size = 100) {
        try {
            const response = await api.get('/teams/my-teams', {
                params: { page, size }
            })
            if (response.data.success && response.data.data) {
                // 返回完整的分页信息，包括 teams 数组和分页元数据
                return {
                    teams: response.data.data.teams || [],
                    totalPages: response.data.data.totalPages || 0,
                    totalElements: response.data.data.totalElements || 0,
                    currentPage: response.data.data.currentPage || page,
                    pageSize: response.data.data.pageSize || size
                }
            }
            return { teams: [], totalPages: 0, totalElements: 0, currentPage: page, pageSize: size }
        } catch (error) {
            console.error('获取我的团队列表失败:', error)
            return { teams: [], totalPages: 0, totalElements: 0, currentPage: page, pageSize: size }
        }
    },

    async createTeam(data) {
        try {
            const response = await api.post('/teams', data)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '创建战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '创建战队失败')
        }
    },

    async hasTeamForCompetition(competitionId) {
        try {
            // 确保 competitionId 是有效的数字
            const params = {}
            if (competitionId && competitionId !== 'undefined' && competitionId !== 'null') {
                const numId = parseInt(competitionId, 10)
                if (!isNaN(numId)) {
                    params.competitionId = numId
                }
            }
            const response = await api.get('/teams/my-team', { params })
            if (response.data.success && response.data.data) {
                if (response.data.data.hasTeam === false) {
                    return false
                }
                return true
            }
            return false
        } catch (error) {
            return false
        }
    },

    async getTeams(page = 0, size = 20, competitionId = null, auditState = null, keyword = null) {
        try {
            const params = { page, size }
            // 确保 competitionId 是有效的数字
            if (competitionId && competitionId !== 'undefined' && competitionId !== 'null') {
                const numId = parseInt(competitionId, 10)
                if (!isNaN(numId)) {
                    params.competitionId = numId
                }
            }
            if (auditState) params.auditState = auditState
            if (keyword) params.keyword = keyword
            const response = await api.get('/teams', { params })
            console.log('团队API响应:', response.data)
            if (response.data.success && response.data.data) {
                // 确保返回的数据结构正确
                const data = response.data.data
                if (data.teams && Array.isArray(data.teams)) {
                    return data
                } else if (Array.isArray(data)) {
                    // 如果直接返回数组，包装成对象
                    return { teams: data, totalPages: 1, totalElements: data.length, currentPage: 0 }
                }
                return { teams: [], totalPages: 0, totalElements: 0, currentPage: 0 }
            }
            return { teams: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            console.error('获取团队列表错误:', error)
            throw new Error(error.response?.data?.message || error.message || '获取战队列表失败')
        }
    },

    async getTeamById(teamId) {
        try {
            const response = await api.get(`/teams/${teamId}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取战队详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取战队详情失败')
        }
    },

    async updateTeam(teamId, teamData) {
        try {
            const response = await api.put(`/teams/${teamId}`, teamData)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '更新战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '更新战队失败')
        }
    },

    async disbandTeam(teamId) {
        try {
            const response = await api.delete(`/teams/${teamId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '解散战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '解散战队失败')
        }
    },

    async joinTeam(teamId, remark = null) {
        try {
            const response = await api.post(`/teams/${teamId}/apply`, { remark })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '申请加入战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '申请加入战队失败')
        }
    },

    async leaveTeam(teamId) {
        try {
            const response = await api.post(`/teams/${teamId}/leave`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '离开战队失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '离开战队失败')
        }
    },

    async processApplication(applicationId, approved, remark = null) {
        try {
            const response = await api.post(`/teams/applications/${applicationId}/process`, {
                approved,
                remark
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '处理申请失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '处理申请失败')
        }
    },

    async getTeamApplications(teamId, page = 0, size = 20, status = null) {
        try {
            const params = { page, size }
            if (status) params.status = status
            const response = await api.get(`/teams/${teamId}/applications`, { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { applications: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取申请列表失败')
        }
    },

    async getMyApplications(page = 0, size = 20) {
        try {
            const params = { page, size }
            const response = await api.get('/teams/my-applications', { params })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { applications: [], totalPages: 0, totalElements: 0, currentPage: 0 }
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取我的申请失败')
        }
    },

    async transferCaptain(teamId, newCaptainId) {
        try {
            const response = await api.post(`/teams/${teamId}/transfer-captain`, {
                newCaptainId
            })
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '转让队长权限失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '转让队长权限失败')
        }
    },

    async removeMember(teamId, memberId) {
        try {
            const response = await api.delete(`/teams/${teamId}/members/${memberId}`)
            if (response.data.success) {
                return true
            }
            throw new Error(response.data.message || '移除成员失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '移除成员失败')
        }
    },

    async auditTeam(teamId, auditState, auditRemark = null) {
        try {
            const response = await api.post(`/teams/${teamId}/audit`, {
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

    async canJoinTeam(teamId) {
        try {
            const response = await api.get(`/teams/${teamId}/can-join`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return { canJoin: false }
        } catch (error) {
            return { canJoin: false }
        }
    },

    async getTeamStatistics(teamId) {
        try {
            const response = await api.get(`/teams/${teamId}/stats`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return {}
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取战队统计失败')
        }
    },

    async inviteUser(teamId, targetUserId) {
        try {
            const response = await api.post(`/teams/${teamId}/invite`, {
                targetUserId
            })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '邀请用户失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '邀请用户失败')
        }
    }
}

