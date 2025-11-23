import api from './apiService'

export const competitionService = {
    async getPublishedCompetitions(page = 0, size = 20, type = 'public', keyword = null) {
        try {
            const params = { page, size, type }
            if (keyword) {
                params.keyword = keyword
            }
            const response = await api.get('/competitions', { params })
            if (response.data.success && response.data.data) {
                const competitions = response.data.data.competitions || []
                // 确保只返回 isPublic 为 true 或 1 的竞赛
                return competitions.filter(comp => {
                    const isPublic = comp.isPublic !== undefined ? comp.isPublic : comp.ispublic
                    return isPublic === true || isPublic === 1 || isPublic === '1'
                })
            }
            return []
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取竞赛列表失败')
        }
    },

    async getCompetitionById(id) {
        try {
            const response = await api.get(`/competitions/${id}`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '获取竞赛详情失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取竞赛详情失败')
        }
    },

    async createCompetition(data) {
        try {
            const response = await api.post('/competitions', data)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || '创建竞赛失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '创建竞赛失败')
        }
    },

    async submitFlag(competitionId, problemId, flag) {
        try {
            // 使用正确的API端点：/api/challenges/{challengeId}/submit
            const response = await api.post(`/challenges/${problemId}/submit`, { flag })
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            throw new Error(response.data.message || 'Flag提交失败')
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || 'Flag提交失败')
        }
    },

    async getDashboardStats() {
        try {
            // 实际项目中这里会调用API
            return {
                activeCompetitions: 3,
                solvedProblems: 12,
                totalScore: 845,
                rank: 15,
                teamRank: 2,
                currentCompetition: '网络安全挑战赛'
            }
        } catch (error) {
            throw new Error('获取仪表盘数据失败')
        }
    },

    async getRecentCompetitions(page = 0, size = 5) {
        try {
            // 获取最近的竞赛（按发布时间排序，取前5个）
            const response = await api.get('/competitions', {
                params: { page, size, type: 'public', sort: 'publishTime' }
            })
            if (response.data.success && response.data.data) {
                const competitions = response.data.data.competitions || []
                
                // 格式化数据并获取统计数据
                const formattedCompetitions = await Promise.all(
                    competitions.map(async (comp) => {
                        // Jackson序列化后字段名是Java属性名（驼峰命名）：competitionID, title, startTime, endTime
                        const competitionId = comp.competitionID || comp.id
                        let participantCount = comp.participants || comp.participantCount || 0
                        
                        // 尝试获取统计数据
                        if (competitionId) {
                            try {
                                // 直接调用 getCompetitionStatistics 方法
                                const statsResponse = await api.get(`/competitions/${competitionId}/statistics`)
                                if (statsResponse.data.success && statsResponse.data.data) {
                                    participantCount = statsResponse.data.data.participantCount || participantCount
                                }
                            } catch (err) {
                                // 忽略统计获取失败
                                console.warn(`获取竞赛 ${competitionId} 统计数据失败:`, err)
                            }
                        }
                        
                        // 后端返回的字段：competitionID, title, startTime, endTime (Jackson序列化的Java属性名)
                        return {
                            id: competitionId,
                            competitionID: competitionId,
                            title: comp.title || '未命名竞赛',
                            startAt: comp.startTime || '',
                            endAt: comp.endTime || '',
                            participants: participantCount
                        }
                    })
                )
                return formattedCompetitions
            }
            return []
        } catch (error) {
            console.error('获取近期竞赛失败:', error)
            throw new Error(error.response?.data?.message || '获取近期竞赛失败')
        }
    },

    async getLeaderboard(competitionId, page = 0, size = 20) {
        try {
            // 确保 competitionId 是有效的数字
            if (!competitionId || competitionId === 'undefined' || competitionId === 'null') {
                throw new Error('无效的竞赛ID')
            }
            const numId = parseInt(competitionId, 10)
            if (isNaN(numId)) {
                throw new Error('无效的竞赛ID')
            }
            const response = await api.get(`/flags/competitions/${numId}/leaderboard`, {
                params: { page, size }
            })
            if (response.data.success && response.data.data) {
                const leaderboardData = response.data.data.leaderboard || response.data.data || []
                console.log('排行榜原始数据:', leaderboardData)
                if (!Array.isArray(leaderboardData)) {
                    console.warn('排行榜数据不是数组:', leaderboardData)
                    return []
                }
                const mapped = leaderboardData.map((item, index) => {
                    const rank = item.rank !== undefined && item.rank !== null ? item.rank : (index + 1)
                    return {
                        id: item.entityID || item.id || item.teamID || index,
                        team: item.name || item.entityName || item.teamName || '未知团队',
                        score: item.totalScore || item.score || 0,
                        rank: rank,
                        solveCount: item.solveCount || 0,
                        entityType: item.entityType || 'TEAM'
                    }
                })
                console.log('处理后的排行榜数据:', mapped)
                return mapped
            }
            return []
        } catch (error) {
            console.error('获取排行榜失败:', error)
            return []
        }
    },

    async getCompetitionStatistics(competitionId) {
        try {
            if (!competitionId || competitionId === 'undefined' || competitionId === 'null') {
                throw new Error('无效的竞赛ID')
            }
            const numId = parseInt(competitionId, 10)
            if (isNaN(numId)) {
                throw new Error('无效的竞赛ID')
            }
            const response = await api.get(`/competitions/${numId}/statistics`)
            if (response.data.success && response.data.data) {
                return response.data.data
            }
            return {}
        } catch (error) {
            console.error('获取竞赛统计失败:', error)
            return {}
        }
    },

    async getUserRank(competitionId, userId) {
        try {
            if (!competitionId || competitionId === 'undefined' || competitionId === 'null') {
                throw new Error('无效的竞赛ID')
            }
            if (!userId) {
                return null
            }
            const numId = parseInt(competitionId, 10)
            if (isNaN(numId)) {
                throw new Error('无效的竞赛ID')
            }
            const response = await api.get(`/scores/users/${userId}/rank`, {
                params: { competitionId: numId }
            })
            if (response.data.success && response.data.data) {
                return response.data.data.rank || null
            }
            return null
        } catch (error) {
            console.error('获取用户排名失败:', error)
            return null
        }
    },

    async getTeamTotalScore(teamId, competitionId) {
        try {
            if (!teamId || !competitionId) {
                return 0
            }
            const response = await api.get(`/scores/teams/${teamId}/total`, {
                params: { competitionId }
            })
            if (response.data.success && response.data.data) {
                return response.data.data.totalScore || 0
            }
            return 0
        } catch (error) {
            console.error('获取队伍总分失败:', error)
            return 0
        }
    },

    // 获取团队排名
    async getTeamRank(teamId, competitionId) {
        try {
            if (!teamId || !competitionId) {
                return null
            }
            const numCompetitionId = parseInt(competitionId, 10)
            if (isNaN(numCompetitionId)) {
                throw new Error('无效的竞赛ID')
            }
            const response = await api.get(`/scores/teams/${teamId}/rank`, {
                params: { competitionId: numCompetitionId }
            })
            if (response.data.success && response.data.data) {
                return response.data.data.rank || null
            }
            return null
        } catch (error) {
            console.error('获取团队排名失败:', error)
            return null
        }
    }
}
