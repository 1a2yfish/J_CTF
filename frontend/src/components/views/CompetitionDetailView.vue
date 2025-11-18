<template>
  <div>
    <t-breadcrumb>
      <t-breadcrumb-item @click="router.push('/competitions')">竞赛列表</t-breadcrumb-item>
      <t-breadcrumb-item>{{ competition.title }}</t-breadcrumb-item>
    </t-breadcrumb>

    <div style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0;">
      <div>
        <h1 style="font-size: 1.8rem; color: #1d2129; margin-bottom: 8px;">{{ competition.title }}</h1>
        <div style="display: flex; align-items: center; color: #646a73;">
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
          <h2 style="margin-bottom: 16px; color: #1d2129;">竞赛描述</h2>
          <p style="line-height: 1.6; color: #4e5969; margin-bottom: 24px;">
            {{ competition.description }}
          </p>

          <h2 style="margin: 32px 0 16px; color: #1d2129;">题目列表</h2>
          <div class="problem-list-container">
            <ProblemCard
                v-for="(problem, index) in competition.problems"
                :key="index"
                :problem="problem"
                @submit-flag="handleFlagSubmit"
            />
          </div>
        </div>
      </div>

      <div>
        <div class="card">
          <h2 style="margin-bottom: 16px; color: #1d2129;">竞赛信息</h2>
          <div style="margin-bottom: 20px;">
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="time" style="color: var(--primary-color); margin-right: 10px;" />
              <div>
                <div style="font-weight: 500;">开始时间</div>
                <div>{{ competition.startAt }}</div>
              </div>
            </div>
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="time" style="color: var(--primary-color); margin-right: 10px;" />
              <div>
                <div style="font-weight: 500;">结束时间</div>
                <div>{{ competition.endAt }}</div>
              </div>
            </div>
            <div style="display: flex; margin-bottom: 12px;">
              <t-icon name="user" style="color: var(--primary-color); margin-right: 10px;" />
              <div>
                <div style="font-weight: 500;">参与人数</div>
                <div>{{ competition.participants }} 人</div>
              </div>
            </div>
            <div style="display: flex;">
              <t-icon name="trophy" style="color: var(--primary-color); margin-right: 10px;" />
              <div>
                <div style="font-weight: 500;">当前排名</div>
                <div>第 {{ competition.rank }} 名</div>
              </div>
            </div>
          </div>

          <h2 style="margin: 32px 0 16px; color: #1d2129;">当前团队</h2>
          <TeamCard
              v-if="hasTeam"
              :team="currentTeam"
              :is-current-team="true"
          />
          <t-empty v-else description="您尚未加入任何团队" style="padding: 30px 0;">
            <template #footer>
              <t-button theme="primary" @click="showJoinTeamDialog = true">创建团队</t-button>
            </template>
          </t-empty>
        </div>

        <div class="card">
          <h2 style="margin-bottom: 16px; color: #1d2129;">排行榜</h2>
          <t-table
              :data="leaderboard"
              :columns="leaderboardColumns"
              row-key="id"
              size="medium"
              style="margin-top: 20px;"
          >
            <template #rank="{ rowIndex }">
              <div :style="{ color: rowIndex < 3 ? '#f5a623' : '' }">
                {{ rowIndex + 1 }}
              </div>
            </template>
          </t-table>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProblemCard from '../common/ProblemCard.vue'
import TeamCard from '../common/TeamCard.vue'
import JoinTeamModal from '../modals/JoinTeamModal.vue'
import { useCompetitionStore } from '@/stores/competitionStore'
import { useTeamStore } from '@/stores/teamStore'
import { Message } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const competitionStore = useCompetitionStore()
const teamStore = useTeamStore()

const competitionId = computed(() => route.params.id)
const competition = ref({
  id: 0,
  title: '',
  description: '',
  startAt: '',
  endAt: '',
  status: '',
  participants: 0,
  rank: 0,
  problems: []
})
const leaderboard = ref([])
const leaderboardColumns = ref([
  { colKey: 'rank', title: '排名', width: 80 },
  { colKey: 'team', title: '团队', width: 150 },
  { colKey: 'score', title: '分数', width: 100 }
])
const hasTeam = ref(false)
const currentTeam = ref(null)
const showJoinTeamDialog = ref(false)

onMounted(async () => {
  try {
    // 加载竞赛详情
    competition.value = await competitionStore.getCompetitionById(competitionId.value)

    // 加载排行榜
    leaderboard.value = await competitionStore.getLeaderboard(competitionId.value)

    // 检查用户是否已加入团队
    hasTeam.value = await teamStore.hasTeamForCompetition(competitionId.value)
    if (hasTeam.value) {
      currentTeam.value = await teamStore.getCurrentTeam(competitionId.value)
    }
  } catch (error) {
    console.error('加载竞赛详情失败:', error)
    Message.error('加载竞赛详情失败')
    await router.push('/competitions')
  }
})

const handleFlagSubmit = async (problemId, flag) => {
  try {
    const result = await competitionStore.submitFlag(competitionId.value, problemId, flag)

    if (result.correct) {
      // 更新问题状态
      const problem = competition.value.problems.find(p => p.id === problemId)
      if (problem) {
        problem.solved = true
      }

      Message.success(`Flag正确！获得 ${problem.score} 分`)
    } else {
      Message.error('Flag错误，请重试')
    }
  } catch (error) {
    Message.error(error.message || '提交失败')
  }
}

const handleTeamJoined = () => {
  hasTeam.value = true
  currentTeam.value = teamStore.getCurrentTeam(competitionId.value)
  showJoinTeamDialog.value = false
}
</script>