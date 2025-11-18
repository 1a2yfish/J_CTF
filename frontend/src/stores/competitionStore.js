import { defineStore } from 'pinia'
import { competitionService } from '../services/competitionService'

export const useCompetitionStore = defineStore('competition', {
    state: () => ({
        competitions: [],
        currentCompetition: null,
        loading: false,
        error: null
    }),

    actions: {
        async getPublishedCompetitions() {
            this.loading = true
            this.error = null

            try {
                const competitions = await competitionService.getPublishedCompetitions()
                this.competitions = competitions
                return competitions
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async getCompetitionById(id) {
            this.loading = true
            this.error = null

            try {
                const competition = await competitionService.getCompetitionById(id)
                this.currentCompetition = competition
                return competition
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async createCompetition(data) {
            this.loading = true
            this.error = null

            try {
                const competition = await competitionService.createCompetition(data)
                this.competitions.push(competition)
                return competition
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async submitFlag(competitionId, problemId, flag) {
            this.loading = true
            this.error = null

            try {
                const result = await competitionService.submitFlag(competitionId, problemId, flag)
                return result
            } catch (error) {
                this.error = error.message
                throw error
            } finally {
                this.loading = false
            }
        },

        async getDashboardStats() {
            try {
                return await competitionService.getDashboardStats()
            } catch (error) {
                console.error('获取仪表盘数据失败:', error)
                return {
                    activeCompetitions: 0,
                    solvedProblems: 0,
                    totalScore: 0,
                    rank: 0,
                    teamRank: 0,
                    currentCompetition: '网络安全挑战赛'
                }
            }
        },

        async getRecentCompetitions() {
            try {
                return await competitionService.getRecentCompetitions()
            } catch (error) {
                console.error('获取近期竞赛失败:', error)
                return []
            }
        },

        async getLeaderboard(competitionId) {
            try {
                return await competitionService.getLeaderboard(competitionId)
            } catch (error) {
                console.error('获取排行榜失败:', error)
                return []
            }
        }
    }
})
