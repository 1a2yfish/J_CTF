<template>
  <div class="leaderboard-page">
    <div class="page-header">
      <div>
        <h1 style="font-size: 1.8rem; color: #e0e6ed; margin-bottom: 8px;">排行榜</h1>
        <div v-if="competition" style="display: flex; align-items: center; color: #9ca3af;">
          <t-icon name="flag" style="margin-right: 8px;" />
          {{ competition.title }}
        </div>
      </div>
      <t-button variant="outline" @click="goBack">
        <t-icon name="arrow-left" style="margin-right: 4px;" />
        返回
      </t-button>
    </div>

    <div class="card">
      <div v-if="loading" style="text-align: center; padding: 40px;">
        <t-loading size="large" />
      </div>
      <div v-else-if="error" style="text-align: center; padding: 40px;">
        <t-alert theme="error" :message="error" />
      </div>
      <div v-else>
        <div v-if="leaderboard.length === 0" class="empty-leaderboard">
          <t-empty description="暂无排行榜数据" />
        </div>
        <t-table
            v-else
            :data="leaderboard"
            :columns="leaderboardColumns"
            row-key="id"
            size="medium"
            :hover="true"
        >
          <template #rank="{ row, rowIndex }">
            <div class="rank-cell" :class="{ 'top-three': (row.rank || rowIndex + 1) <= 3 }">
              <span v-if="(row.rank || rowIndex + 1) === 1" class="rank-icon">🥇</span>
              <span v-else-if="(row.rank || rowIndex + 1) === 2" class="rank-icon">🥈</span>
              <span v-else-if="(row.rank || rowIndex + 1) === 3" class="rank-icon">🥉</span>
              <span v-else>{{ row.rank || rowIndex + 1 }}</span>
            </div>
          </template>
          <template #team="{ row }">
            <div style="font-weight: 500; color: #e0e6ed;">
              {{ row.team || row.name || row.entityName || row.teamName || '未知团队' }}
            </div>
          </template>
          <template #score="{ row }">
            <div style="color: #667eea; font-weight: 600; font-size: 16px;">
              {{ row.score || row.totalScore || 0 }}
            </div>
          </template>
          <template #solveCount="{ row }">
            <div style="color: #9ca3af;">
              {{ row.solveCount || 0 }} 题
            </div>
          </template>
        </t-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCompetitionStore } from '@/stores/competitionStore'
import { MessagePlugin } from 'tdesign-vue-next'
import { showError, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const route = useRoute()
const router = useRouter()
const competitionStore = useCompetitionStore()

const competitionId = computed(() => {
  const id = route.query.competitionId || route.params.id
  if (!id || id === 'undefined' || id === 'null') {
    return null
  }
  const numId = parseInt(id, 10)
  return isNaN(numId) ? null : numId
})

const competition = ref(null)
const leaderboard = ref([])
const loading = ref(false)
const error = ref(null)

const leaderboardColumns = ref([
  { colKey: 'rank', title: '排名', width: 120 },
  { colKey: 'team', title: '团队', width: 300 },
  { colKey: 'score', title: '总分', width: 150 },
  { colKey: 'solveCount', title: '解题数', width: 120 }
])

const loadData = async () => {
  if (!competitionId.value) {
    error.value = '无效的竞赛ID'
    return
  }

  try {
    loading.value = true
    error.value = null

    // 加载竞赛信息
    try {
      const compData = await competitionStore.getCompetitionById(competitionId.value)
      competition.value = {
        id: compData.competitionID || compData.id,
        title: compData.competitionName || compData.title || compData.Title
      }
    } catch (err) {
      logError('加载竞赛信息失败:', err)
    }

    // 加载排行榜
    const leaderboardData = await competitionStore.getLeaderboard(competitionId.value)
    
    if (Array.isArray(leaderboardData) && leaderboardData.length > 0) {
      leaderboard.value = leaderboardData.map((item, index) => {
        const rank = item.rank !== undefined && item.rank !== null ? item.rank : (index + 1)
        return {
          id: item.id || item.entityID || index,
          team: item.team || item.entityName || item.teamName || item.name || '未知团队',
          score: item.score || item.totalScore || 0,
          rank: rank,
          solveCount: item.solveCount || 0
        }
      })
    } else {
      leaderboard.value = []
    }
  } catch (err) {
    console.error('加载排行榜失败:', err)
    error.value = '加载排行榜失败: ' + (err.message || '未知错误')
    MessagePlugin.error(error.value)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (competitionId.value) {
    router.push(`/competitions/${competitionId.value}`)
  } else {
    router.push('/competitions')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.leaderboard-page {
  animation: fadeIn 0.3s ease-in-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card {
  background: #1a1f3a;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  border: 1px solid #2a3458;
}

.empty-leaderboard {
  padding: 60px 20px;
  text-align: center;
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  min-width: 60px;
}

.rank-cell.top-three {
  font-size: 20px;
}

.rank-icon {
  font-size: 24px;
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

