import api from './apiService'

export const competitionService = {
    async getPublishedCompetitions() {
        try {
            const response = await api.get('/competitions')
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取竞赛列表失败')
        }
    },

    async getCompetitionById(id) {
        try {
            const response = await api.get(`/competitions/${id}`)
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取竞赛详情失败')
        }
    },

    async createCompetition(data) {
        try {
            const response = await api.post('/competitions', data)
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '创建竞赛失败')
        }
    },

    async submitFlag(competitionId, problemId, flag) {
        try {
            const response = await api.post('/submissions', {
                teamId: localStorage.getItem('ctf_team_id'),
                problemId,
                flag
            })
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Flag提交失败')
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

    async getRecentCompetitions() {
        try {
            // 实际项目中这里会调用API
            return [
                {
                    title: 'CTF新手训练营',
                    startAt: '2023-10-15 10:00',
                    endAt: '2023-10-17 22:00'
                },
                {
                    title: '2023网络安全挑战赛',
                    startAt: '2023-10-01 09:00',
                    endAt: '2023-10-03 18:00'
                },
                {
                    title: '企业级安全攻防赛',
                    startAt: '2023-09-20 08:00',
                    endAt: '2023-09-22 20:00'
                }
            ]
        } catch (error) {
            throw new Error('获取近期竞赛失败')
        }
    },

    async getLeaderboard(competitionId) {
        try {
            // 实际项目中这里会调用API
            return [
                { id: 1, team: '零日漏洞', score: 1250, rank: 1 },
                { id: 2, team: '安全先锋队', score: 1180, rank: 2 },
                { id: 3, team: '代码猎人', score: 950, rank: 3 },
                { id: 4, team: '网络卫士', score: 870, rank: 4 },
                { id: 5, team: '极客联盟', score: 760, rank: 5 }
            ]
        } catch (error) {
            throw new Error('获取排行榜失败')
        }
    }
}
