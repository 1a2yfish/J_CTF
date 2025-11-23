<template>
  <div class="competition-detail-page">

    <div style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0;">
      <div>
        <h1 style="font-size: 1.8rem; color: #e0e6ed; margin-bottom: 8px;">{{ competition.title }}</h1>
        <div style="display: flex; align-items: center; color: #9ca3af;">
          <t-icon name="time" style="margin-right: 8px;" />
          {{ competition.startAt }} 至 {{ competition.endAt }}
          <span style="margin: 0 12px;">|</span>
          <t-icon name="user" style="margin-right: 8px;" />
          {{ competition.participants }} 人参与
        </div>
      </div>
      <t-button
          theme="primary"
          size="large"
          :disabled="competition.status !== 'active'"
          @click="showJoinTeamDialog = true"
      >
        {{ hasTeam ? '管理团队' : '加入/创建团队' }}
      </t-button>
    </div>

    <div class="competition-detail-container">
      <div>
        <div class="card">
          <h2 style="margin-bottom: 16px; color: #e0e6ed;">竞赛描述</h2>
          <p style="line-height: 1.6; color: #d1d5db; margin-bottom: 24px;">
            {{ competition.description }}
          </p>

          <!-- 排行榜卡片 -->
          <div class="leaderboard-entry-card" @click="viewLeaderboard">
            <div class="leaderboard-entry-content">
              <div class="leaderboard-entry-icon">
                <t-icon name="trophy" size="32" />
              </div>
              <div class="leaderboard-entry-info">
                <h3 style="margin: 0 0 8px 0; color: #e0e6ed; font-size: 18px;">查看排行榜</h3>
                <p style="margin: 0; color: #9ca3af; font-size: 14px;">
                  点击查看完整的团队排名和得分情况
                </p>
              </div>
              <t-button
                theme="primary"
                variant="outline"
                size="large"
                @click.stop="viewLeaderboard"
                :ripple="false"
                style="min-width: 120px;"
              >
                <t-icon name="arrow-right" style="margin-right: 4px;" />
                查看
              </t-button>
            </div>
          </div>

          <!-- 进入挑战卡片 -->
          <div class="challenge-entry-card">
            <div class="challenge-entry-content">
              <div class="challenge-entry-icon">
                <t-icon name="file-code" size="32" />
              </div>
              <div class="challenge-entry-info">
                <h3 style="margin: 0 0 8px 0; color: #e0e6ed; font-size: 18px;">开始挑战</h3>
                <p style="margin: 0; color: #9ca3af; font-size: 14px;">
                  点击进入挑战页面，查看所有题目并开始答题
                </p>
              </div>
              <t-button
                theme="primary"
                size="large"
                @click.stop="enterChallenges"
                :ripple="false"
                style="min-width: 120px;"
              >
                <t-icon name="arrow-right" style="margin-right: 4px;" />
                进入挑战
              </t-button>
            </div>
          </div>
        </div>
      </div>

      <div>
        <div class="card">
          <h2 style="margin-bottom: 16px; color: #e0e6ed;">竞赛信息</h2>
          <div style="margin-bottom: 20px;">
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="time" style="color: #3b82f6; margin-right: 10px;" />
              <div>
                <div style="font-weight: 500; color: #e0e6ed;">开始时间</div>
                <div style="color: #9ca3af;">{{ competition.startAt }}</div>
              </div>
            </div>
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="time" style="color: #3b82f6; margin-right: 10px;" />
              <div>
                <div style="font-weight: 500; color: #e0e6ed;">结束时间</div>
                <div style="color: #9ca3af;">{{ competition.endAt }}</div>
              </div>
            </div>
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="user" style="color: #3b82f6; margin-right: 10px;" />
              <div>
                <div style="font-weight: 500; color: #e0e6ed;">参与人数</div>
                <div style="color: #9ca3af;">{{ competition.participants }} 人</div>
              </div>
            </div>
            <div style="display: flex;">
              <t-icon name="trophy" style="color: #3b82f6; margin-right: 10px;" />
              <div>
                <div style="font-weight: 500; color: #e0e6ed;">当前排名</div>
                <div style="color: #9ca3af;" v-if="competition.rank > 0">第 {{ competition.rank }} 名</div>
                <div style="color: #9ca3af;" v-else>未上榜</div>
              </div>
            </div>
          </div>

          <h2 style="margin: 32px 0 16px; color: #e0e6ed;">当前团队</h2>
          <TeamCard
              v-if="hasTeam && currentTeam"
              :team="currentTeam"
              :is-current-team="true"
          />
          <t-empty v-else description="您尚未加入任何团队" style="padding: 30px 0;">
            <template #footer>
              <t-button theme="primary" @click="showJoinTeamDialog = true">创建团队</t-button>
            </template>
          </t-empty>
        </div>

      </div>
    </div>

    <JoinTeamModal
        :visible="showJoinTeamDialog"
        :competition-id="competition.id"
        @close="showJoinTeamDialog = false"
        @team-joined="handleTeamJoined"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TeamCard from '../team/TeamCard.vue'
import JoinTeamModal from '../modals/JoinTeamModal.vue'
import { useCompetitionStore } from '@/stores/competitionStore'
import { useTeamStore } from '@/stores/teamStore'
import { MessagePlugin } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const competitionStore = useCompetitionStore()
const teamStore = useTeamStore()

const competitionId = computed(() => {
  const id = route.params.id
  // 确保返回的是有效的数字，而不是 undefined 或字符串 "undefined"
  if (!id || id === 'undefined' || id === 'null') {
    return null
  }
  // 尝试转换为数字
  const numId = parseInt(id, 10)
  return isNaN(numId) ? null : numId
})
const competition = ref({
  id: 0,
  title: '',
  description: '',
  startAt: '',
  endAt: '',
  status: '',
  participants: 0,
  rank: 0
})
const hasTeam = ref(false)
const currentTeam = ref(null)
const showJoinTeamDialog = ref(false)

onMounted(async () => {
  try {
    // 验证 competitionId 是否有效
    if (!competitionId.value) {
      MessagePlugin.error('无效的竞赛ID')
      await router.push('/competitions')
      return
    }

    // 加载竞赛详情
    const compData = await competitionStore.getCompetitionById(competitionId.value)
    
    // 加载竞赛统计信息（包含参与人数）
    let stats = {}
    try {
      const { competitionService } = await import('@/services/competitionService')
      stats = await competitionService.getCompetitionStatistics(competitionId.value)
    } catch (err) {
      console.error('加载竞赛统计失败:', err)
    }
    
    // 计算竞赛状态（根据时间）
    const startTime = compData.startTime || compData.startAt || compData.StartTime
    const endTime = compData.endTime || compData.endAt || compData.EndTime
    let computedStatus = compData.status || compData.Status || 'active'
    if (startTime && endTime) {
      try {
        const now = new Date()
        const start = new Date(startTime)
        const end = new Date(endTime)
        if (now < start) {
          computedStatus = 'upcoming'
        } else if (now >= start && now <= end) {
          computedStatus = 'ongoing'
        } else {
          computedStatus = 'finished'
        }
      } catch (e) {
        console.error('计算竞赛状态失败:', e)
      }
    }
    
    const compId = compData.competitionID || compData.id
    competition.value = {
      id: compId,
      title: compData.competitionName || compData.title || compData.Title,
      description: compData.description || compData.introduction || compData.Introduction || '',
      startAt: compData.startTime || compData.startAt || compData.StartTime,
      endAt: compData.endTime || compData.endAt || compData.EndTime,
      status: computedStatus,
      participants: stats.participantCount || compData.participantCount || 0,
      rank: 0,
      problems: []
    }
    
    // 确保 competitionId 有值，如果没有则使用已加载的竞赛ID
    if (!competitionId.value && compId) {
      // 如果 computed 返回 null，但我们已经加载了竞赛数据，可以继续
      console.warn('路由参数中的竞赛ID无效，但已加载竞赛数据，ID:', compId)
    }
    
    // 检查用户是否已加入团队
    hasTeam.value = await teamStore.hasTeamForCompetition(competitionId.value)
    if (hasTeam.value) {
      const team = await teamStore.getCurrentTeam(competitionId.value)
      if (team) {
        currentTeam.value = team
        
        // 获取团队排名
        try {
          const teamId = team.teamID || team.id || team.teamId
          if (teamId) {
            const { competitionService } = await import('@/services/competitionService')
            const teamRank = await competitionService.getTeamRank(teamId, competitionId.value)
            if (teamRank) {
              competition.value.rank = teamRank
            } else {
              competition.value.rank = 0 // 如果团队不在排行榜中，显示0
            }
          }
        } catch (err) {
          console.error('获取团队排名失败:', err)
          competition.value.rank = 0
        }
      } else {
        // 如果 hasTeam 为 true 但 getCurrentTeam 返回 null，重置 hasTeam
        hasTeam.value = false
        currentTeam.value = null
        competition.value.rank = 0
      }
    } else {
      currentTeam.value = null
      competition.value.rank = 0
    }

    // 不再在详情页加载排行榜，改为在独立页面加载
  } catch (error) {
    console.error('加载竞赛详情失败:', error)
    MessagePlugin.error('加载竞赛详情失败: ' + (error.message || '未知错误'))
    await router.push('/competitions')
  }
})

const handleTeamJoined = async () => {
  hasTeam.value = true
  currentTeam.value = await teamStore.getCurrentTeam(competitionId.value)
  showJoinTeamDialog.value = false
}

// 查看排行榜
const viewLeaderboard = async () => {
  try {
    const id = competitionId.value || competition.value?.id
    if (!id) {
      MessagePlugin.error('无法获取竞赛ID')
      return
    }
    await router.push({
      path: '/leaderboard',
      query: { competitionId: id }
    })
  } catch (error) {
    console.error('导航失败:', error)
  }
}

// 进入挑战页面
const enterChallenges = async (event) => {
  // 阻止事件冒泡和默认行为
  if (event) {
    event.stopPropagation()
    event.preventDefault()
  }
  
  try {
    // 优先使用 competitionId.value，如果为 null 则使用 competition.id
    const id = competitionId.value || competition.value?.id
    if (!id) {
      console.error('竞赛ID无效', { 
        competitionId: competitionId.value, 
        competition: competition.value,
        routeParams: route.params 
      })
      MessagePlugin.error('无法获取竞赛ID，请刷新页面重试')
      return
    }
    
    // 添加短暂延迟，让按钮的 ripple 效果完成
    await new Promise(resolve => setTimeout(resolve, 150))
    
    // 使用 nextTick 确保在下一个事件循环中执行导航，避免组件卸载时的冲突
    await nextTick()
    
    await router.push({
      path: '/problems',
      query: { competitionId: id }
    })
  } catch (error) {
    // 忽略导航取消的错误（用户快速点击或组件卸载时）
    if (error.name !== 'NavigationDuplicated' && !error.message?.includes('Navigation cancelled')) {
      console.error('导航失败:', error)
    }
  }
}
</script>

<style scoped>
.competition-detail-page {
  animation: fadeIn 0.3s ease-in-out;
}

.leaderboard-entry-card {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(255, 152, 0, 0.05) 100%);
  border: 2px solid rgba(255, 193, 7, 0.3);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.leaderboard-entry-card:hover {
  border-color: rgba(255, 193, 7, 0.5);
  box-shadow: 0 4px 16px rgba(255, 193, 7, 0.2);
  transform: translateY(-2px);
}

.leaderboard-entry-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.leaderboard-entry-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #ffc107 0%, #ff9800 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.leaderboard-entry-info {
  flex: 1;
}

.challenge-entry-card {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  border: 2px solid rgba(59, 130, 246, 0.3);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  transition: all 0.3s ease;
}

.challenge-entry-card:hover {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.challenge-entry-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.challenge-entry-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.challenge-entry-info {
  flex: 1;
}

.more-problems-hint {
  text-align: center;
  padding: 20px;
  margin-top: 16px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>