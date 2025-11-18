<template>
  <div>
    <div class="page-title">仪表盘</div>

    <div class="dashboard-grid">
      <StatCard
          title="参与竞赛"
          :value="stats.activeCompetitions"
          description="当前参与的竞赛数量"
      />
      <StatCard
          title="解决题目"
          :value="stats.solvedProblems"
          description="已解决的题目数量"
      />
      <StatCard
          title="总积分"
          :value="stats.totalScore"
          description="当前总积分排名 #{{ stats.rank }}"
      />
      <StatCard
          title="团队排名"
          :value="stats.teamRank"
          :description="'在' + stats.currentCompetition + '中排名第' + stats.teamRank"
      />
    </div>

    <div class="card">
      <div class="page-title" style="margin: 0 0 20px 0; font-size: 1.2rem;">近期竞赛</div>
      <div class="competition-timeline">
        <div class="timeline-item" v-for="(comp, index) in recentCompetitions" :key="index">
          <div class="timeline-content">
            <div class="timeline-title">{{ comp.title }}</div>
            <div class="timeline-time">
              <t-icon name="time" /> {{ comp.startAt }} 至 {{ comp.endAt }}
            </div>
            <div class="meta-item" style="margin-top: 8px;">
              <t-icon name="user" /> {{ comp.participants }} 人参与
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import StatCard from '../common/StatCard.vue'
import { useCompetitionStore } from '@/stores/competitionStore'

const competitionStore = useCompetitionStore()
const recentCompetitions = ref([])

// 统计数据
const stats = ref({
  activeCompetitions: 0,
  solvedProblems: 0,
  totalScore: 0,
  rank: 0,
  teamRank: 0,
  currentCompetition: ''
})

onMounted(async () => {
  try {
    // 获取统计数据
    const statsData = await competitionStore.getDashboardStats()
    stats.value = statsData

    // 获取近期竞赛
    recentCompetitions.value = await competitionStore.getRecentCompetitions()
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
})
</script>