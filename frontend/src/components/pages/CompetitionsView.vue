<template>
  <div class="competitions-page">
    <div class="page-header">
      <h1 class="page-title">竞赛列表</h1>
      <p class="page-subtitle">浏览所有可参与的竞赛</p>
    </div>
    <div class="card">
      <div v-if="competitions.length === 0" class="empty-state">
        <t-empty description="暂无竞赛数据" />
      </div>
      <div v-else class="competitions-grid">
        <CompetitionCard
            v-for="(competition, index) in competitions"
            :key="competition.competitionID || competition.id || index"
            :competition="competition"
            @click="navigateToCompetition(competition.competitionID || competition.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CompetitionCard from '../competition/CompetitionCard.vue'
import { useCompetitionStore } from '../../stores/competitionStore'
import { competitionService } from '../../services/competitionService'

const router = useRouter()
const competitionStore = useCompetitionStore()
const competitions = ref([])

// 为竞赛列表加载统计数据（参赛人数）
const loadCompetitionsWithStats = async (competitionList) => {
  const competitionsWithStats = await Promise.all(
    competitionList.map(async (competition) => {
      const competitionId = competition.competitionID || competition.id
      if (competitionId) {
        try {
          const stats = await competitionService.getCompetitionStatistics(competitionId)
          return {
            ...competition,
            participants: stats.participantCount || competition.participants || competition.participantCount || 0
          }
        } catch (error) {
          console.error(`获取竞赛 ${competitionId} 统计数据失败:`, error)
          return {
            ...competition,
            participants: competition.participants || competition.participantCount || 0
          }
        }
      }
      return competition
    })
  )
  return competitionsWithStats
}

onMounted(async () => {
  try {
    const competitionList = await competitionStore.getPublishedCompetitions()
    // 加载统计数据
    competitions.value = await loadCompetitionsWithStats(competitionList)
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
})

const navigateToCompetition = (id) => {
  // 确保ID有效
  if (!id || id === 'undefined' || id === 'null') {
    console.error('无效的竞赛ID:', id)
    return
  }
  router.push(`/competitions/${id}`)
}
</script>

<style scoped>
.competitions-page {
  animation: fadeIn 0.3s ease-in-out;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #e0e6ed;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.competitions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

@media (max-width: 768px) {
  .competitions-grid {
    grid-template-columns: 1fr;
  }
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