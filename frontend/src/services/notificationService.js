import api from './apiService'

export const notificationService = {
    // 获取用户的所有通知
    async getAllNotifications() {
        try {
            const notifications = []
            
            // 这里会整合各种通知源
            // 1. Flag提交通知
            // 2. Hint更新通知
            // 3. 队伍审核通知
            // 4. 成员加入请求通知
            
            return notifications
        } catch (error) {
            throw new Error(error.response?.data?.message || error.message || '获取通知失败')
        }
    },

    // 获取Flag提交通知
    async getFlagSubmissionNotifications(userId) {
        try {
            const response = await api.get('/flags/my-submissions', {
                params: { page: 0, size: 50 }
            })
            if (response.data.success && response.data.data) {
                const submissions = response.data.data.submissions || []
                return submissions.map(sub => ({
                    id: `flag-${sub.submissionID || sub.id}`,
                    type: 'flag_submission',
                    title: sub.isCorrect ? 'Flag提交成功' : 'Flag提交失败',
                    message: sub.isCorrect 
                        ? `恭喜！您提交的Flag正确，获得 ${sub.pointsAwarded || 0} 分`
                        : '您提交的Flag不正确，请重试',
                    time: sub.submitTime || sub.submitTime || new Date(),
                    icon: sub.isCorrect ? 'check-circle' : 'close-circle',
                    read: false,
                    submissionId: sub.submissionID || sub.id,
                    challengeId: sub.challenge?.challengeID || sub.challengeId,
                    isCorrect: sub.isCorrect
                }))
            }
            return []
        } catch (error) {
            console.error('获取Flag提交通知失败:', error)
            return []
        }
    },

    // 获取Hint更新通知（需要检查用户关注的题目是否有新提示）
    async getHintUpdateNotifications(userId) {
        try {
            // 由于没有直接的API，这里返回空数组
            // 实际实现中可能需要：
            // 1. 获取用户已查看的题目列表
            // 2. 检查这些题目的提示是否有更新
            return []
        } catch (error) {
            console.error('获取Hint更新通知失败:', error)
            return []
        }
    },

    // 获取队伍审核通知
    async getTeamAuditNotifications(userId) {
        try {
            // 获取所有团队，然后过滤出用户作为队长的团队
            const response = await api.get('/teams', {
                params: { page: 0, size: 100 }
            })
            
            if (response.data.success && response.data.data) {
                const teams = response.data.data.teams || response.data.data || []
                // 过滤出用户作为队长的团队
                const myTeams = teams.filter(team => {
                    const captainId = team.captain?.userID || team.captain?.userId || 
                                     (team.captain && typeof team.captain === 'object' ? team.captain.userID || team.captain.userId : null) ||
                                     (typeof team.captain === 'number' ? team.captain : null)
                    return captainId === userId
                })
                
                // 只返回已通过审核的团队通知
                return myTeams
                    .filter(team => team.auditState === '1' || team.auditState === 'APPROVED')
                    .map(team => ({
                        id: `team-audit-${team.teamID || team.id}`,
                        type: 'team_audit',
                        title: '队伍审核通过',
                        message: `您的队伍 "${team.teamName || team.name}" 已通过审核`,
                        time: team.auditTime || team.creationTime || new Date(),
                        icon: 'check-circle',
                        read: false,
                        teamId: team.teamID || team.id,
                        teamName: team.teamName || team.name
                    }))
            }
            return []
        } catch (error) {
            console.error('获取队伍审核通知失败:', error)
            return []
        }
    },

    // 获取成员加入请求通知（作为队长）
    async getMemberJoinRequestNotifications(userId) {
        try {
            // 获取所有团队，然后过滤出用户作为队长的团队
            const response = await api.get('/teams', {
                params: { page: 0, size: 100 }
            })
            
            if (response.data.success && response.data.data) {
                const teams = response.data.data.teams || response.data.data || []
                // 过滤出用户作为队长的团队
                const myTeams = teams.filter(team => {
                    const captainId = team.captain?.userID || team.captain?.userId || 
                                     (team.captain && typeof team.captain === 'object' ? team.captain.userID || team.captain.userId : null) ||
                                     (typeof team.captain === 'number' ? team.captain : null)
                    return captainId === userId
                })
                
                const notifications = []
                
                // 获取每个团队的待处理申请
                for (const team of myTeams) {
                    try {
                        const teamId = team.teamID || team.id
                        const appResponse = await api.get(`/teams/${teamId}/applications`, {
                            params: { status: 'PENDING', page: 0, size: 50 }
                        })
                        
                        if (appResponse.data.success && appResponse.data.data) {
                            const applications = appResponse.data.data.applications || []
                            applications.forEach(app => {
                                const applicantName = app.applicant?.userName || app.applicant?.name || 
                                                    app.applicantName || '用户'
                                notifications.push({
                                    id: `join-request-${app.applicationID || app.id}`,
                                    type: 'member_join_request',
                                    title: '成员加入请求',
                                    message: `${applicantName} 申请加入您的队伍 "${team.teamName || team.name}"`,
                                    time: app.applyTime || new Date(),
                                    icon: 'user-add',
                                    read: false,
                                    applicationId: app.applicationID || app.id,
                                    teamId: teamId,
                                    teamName: team.teamName || team.name,
                                    applicantId: app.applicant?.userID || app.applicant?.userId || app.applicantID,
                                    applicantName: applicantName,
                                    remark: app.remark || ''
                                })
                            })
                        }
                    } catch (err) {
                        console.error(`获取团队 ${team.teamID || team.id} 的申请失败:`, err)
                    }
                }
                
                return notifications
            }
            return []
        } catch (error) {
            console.error('获取成员加入请求通知失败:', error)
            return []
        }
    }
}

