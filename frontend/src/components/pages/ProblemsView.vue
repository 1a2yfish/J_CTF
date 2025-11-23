<template>
  <div class="problems-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          {{ currentCompetition ? currentCompetition.title || currentCompetition.Title : '题目列表' }}
        </h1>
        <p class="page-subtitle">
          {{ currentCompetition ? '浏览该竞赛的所有题目，按分类和难度筛选' : '浏览所有题目，按分类和难度筛选' }}
        </p>
        <div v-if="currentCompetition" class="competition-info">
          <t-tag theme="primary" style="margin-top: 8px;">
            <t-icon name="flag" style="margin-right: 4px;" />
            {{ currentCompetition.title || currentCompetition.Title }}
          </t-tag>
        </div>
      </div>
    </div>

    <!-- 队伍信息卡片（仅在有competitionId时显示） -->
    <div v-if="competitionIdFromRoute && (currentTeam || teamTotalScore !== null)" class="card team-info-card">
      <div class="team-info-content">
        <div v-if="currentTeam" class="team-info-section">
          <div class="team-info-header">
            <t-icon name="users" style="color: #3b82f6; font-size: 20px; margin-right: 8px;" />
            <h3 style="margin: 0; color: #e0e6ed;">我的队伍</h3>
          </div>
          <div class="team-info-details">
            <div class="team-detail-item">
              <span class="team-detail-label">队伍名称：</span>
              <span class="team-detail-value">{{ currentTeam.teamName || currentTeam.name || '未知' }}</span>
            </div>
            <div class="team-detail-item" v-if="currentTeam.captainName || currentTeam.captain?.userName">
              <span class="team-detail-label">队长：</span>
              <span class="team-detail-value">{{ currentTeam.captainName || currentTeam.captain?.userName }}</span>
            </div>
            <div class="team-detail-item" v-if="currentTeam.members">
              <span class="team-detail-label">成员数：</span>
              <span class="team-detail-value">{{ currentTeam.members.length || 0 }} 人</span>
            </div>
          </div>
        </div>
        <div v-if="teamTotalScore !== null" class="team-score-section">
          <div class="team-score-header">
            <t-icon name="trophy" style="color: #fbbf24; font-size: 20px; margin-right: 8px;" />
            <h3 style="margin: 0; color: #e0e6ed;">队伍总分</h3>
          </div>
          <div class="team-score-value">
            <span class="score-number">{{ teamTotalScore }}</span>
            <span class="score-unit">分</span>
          </div>
        </div>
        <div v-if="!currentTeam && competitionIdFromRoute" class="team-info-empty">
          <t-empty description="您尚未加入该竞赛的队伍" style="padding: 20px 0;">
            <template #footer>
              <t-button theme="primary" @click="$router.push('/teams')">前往加入队伍</t-button>
            </template>
          </t-empty>
        </div>
      </div>
    </div>

    <div class="card">
      <!-- 搜索和筛选栏 -->
      <div class="filter-bar">
        <!-- 当没有competitionId时显示搜索框和竞赛选择 -->
        <template v-if="!competitionIdFromRoute">
          <t-input
            v-model="searchKeyword"
            placeholder="搜索题目..."
            clearable
            style="width: 300px;"
            @enter="loadProblems"
          >
            <template #prefix-icon>
              <t-icon name="search" />
            </template>
          </t-input>
        </template>
        <div class="filter-actions">
          <!-- 当没有competitionId时显示竞赛选择 -->
          <t-select
            v-if="!competitionIdFromRoute"
            v-model="selectedCompetition"
            placeholder="选择竞赛"
            clearable
            style="width: 200px;"
            @change="loadProblems"
          >
            <t-option
              v-for="comp in competitions"
              :key="comp.competitionID || comp.id"
              :value="comp.competitionID || comp.id"
              :label="comp.title || comp.Title"
            />
          </t-select>
          <t-select
            v-model="selectedDifficulty"
            placeholder="选择难度"
            clearable
            style="width: 150px;"
            @change="loadProblems"
          >
            <t-option value="Easy" label="简单" />
            <t-option value="Medium" label="中等" />
            <t-option value="Hard" label="困难" />
          </t-select>
          <t-button @click="loadProblems">刷新</t-button>
        </div>
      </div>

      <!-- 分类标签 -->
      <div class="category-tags" v-if="categories.length > 0">
        <div class="category-tags-header">
          <span class="category-label">分类筛选：</span>
        </div>
        <div class="category-tags-list">
          <t-tag
            v-for="cat in categories"
            :key="cat"
            :theme="selectedCategory === cat ? 'primary' : 'default'"
            :closable="false"
            class="category-tag"
            @click="toggleCategory(cat)"
          >
            {{ cat }}
          </t-tag>
          <t-tag
            v-if="selectedCategory"
            theme="default"
            closable
            class="category-tag"
            @close="selectedCategory = null; loadProblems()"
          >
            清除筛选
          </t-tag>
        </div>
      </div>

      <!-- 题目列表 -->
      <div class="problems-list">
        <div v-if="loading" class="loading-container">
          <t-loading :loading="loading" text="加载中..." />
        </div>
        <div v-else-if="problems.length === 0" class="empty-container">
          <t-empty description="暂无题目数据" />
        </div>
        <div v-else class="problems-grid">
          <ProblemCard
            v-for="problem in problems"
            :key="problem.challengeID || problem.id"
            :problem="problem"
            :competition-status="currentCompetition?.status || 'ongoing'"
            :can-submit="isCompetitionOngoing"
            @submit-flag="handleFlagSubmit"
            @view-hints="handleViewHints"
          />
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalPages > 1">
        <t-pagination
          v-model="currentPage"
          :total="totalElements"
          :page-size="pageSize"
          :show-jumper="true"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- 提示对话框 -->
    <t-dialog
      v-model:visible="showHintsDialog"
      :header="currentProblemTitle + ' - 提示'"
      width="500px"
    >
      <div class="hints-list">
        <div
          v-for="(hint, index) in currentHints"
          :key="index"
          class="hint-item"
        >
          <div class="hint-number">提示 {{ index + 1 }}</div>
          <div class="hint-content">{{ hint.content || hint.hintContent || hint.Content }}</div>
        </div>
        <div v-if="currentHints.length === 0" class="empty-hints">
          暂无提示
        </div>
      </div>
      <template #footer>
        <t-button theme="primary" @click="showHintsDialog = false">关闭</t-button>
      </template>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { problemService } from '@/services/problemService'
import { competitionService } from '@/services/competitionService'
import { teamService } from '@/services/teamService'
import ProblemCard from '../challenge/ProblemCard.vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { showSuccess, showError, showWarning, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const route = useRoute()
const router = useRouter()

const problems = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref(null)
const selectedDifficulty = ref(null)
const selectedCompetition = ref(null)
const competitions = ref([])
const currentCompetition = ref(null)
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalPages = ref(0)
const totalElements = ref(0)

const showHintsDialog = ref(false)
const currentHints = ref([])
const currentProblemTitle = ref('')

// 队伍信息
const currentTeam = ref(null)
const teamTotalScore = ref(null)

// 从路由参数获取竞赛ID
const competitionIdFromRoute = computed(() => {
  const id = route.query.competitionId || route.params.competitionId
  if (!id || id === 'undefined' || id === 'null') {
    return null
  }
  const numId = parseInt(id, 10)
  return isNaN(numId) ? null : numId
})

// 检查竞赛是否正在进行
const isCompetitionOngoing = computed(() => {
  if (!currentCompetition.value) {
    return true // 如果没有指定竞赛，默认允许提交
  }
  
  const status = currentCompetition.value.status || currentCompetition.value.Status
  if (status === 'ongoing' || status === 'active') {
    return true
  }
  
  // 根据时间判断
  const startTime = currentCompetition.value.startTime || currentCompetition.value.startAt || currentCompetition.value.StartTime
  const endTime = currentCompetition.value.endTime || currentCompetition.value.endAt || currentCompetition.value.EndTime
  
  if (startTime && endTime) {
    try {
      const now = new Date()
      const start = new Date(startTime)
      const end = new Date(endTime)
      return now >= start && now <= end
    } catch (e) {
      console.error('解析时间失败:', e)
      return true
    }
  }
  
  return true // 默认允许提交
})

// 加载队伍信息和总分
const loadTeamInfo = async () => {
  if (!competitionIdFromRoute.value) {
    currentTeam.value = null
    teamTotalScore.value = null
    return
  }

  try {
    // 加载当前用户的队伍信息
    const team = await teamService.getMyTeam(competitionIdFromRoute.value)
    currentTeam.value = team

    // 如果用户有队伍，加载队伍总分
    if (team && team.teamID) {
      try {
        const totalScore = await competitionService.getTeamTotalScore(team.teamID, competitionIdFromRoute.value)
        teamTotalScore.value = totalScore
      } catch (error) {
        console.error('加载队伍总分失败:', error)
        teamTotalScore.value = 0
      }
    } else {
      teamTotalScore.value = null
    }
  } catch (error) {
    console.error('加载队伍信息失败:', error)
    currentTeam.value = null
    teamTotalScore.value = null
  }
}

// 加载竞赛列表
const loadCompetitions = async () => {
  try {
    const comps = await competitionService.getPublishedCompetitions(0, 100)
    competitions.value = comps || []
    
    // 如果有竞赛ID参数，加载该竞赛的详细信息
    if (competitionIdFromRoute.value) {
      try {
        const compData = await competitionService.getCompetitionById(competitionIdFromRoute.value)
        currentCompetition.value = compData
        selectedCompetition.value = competitionIdFromRoute.value
        // 加载队伍信息
        await loadTeamInfo()
      } catch (error) {
        console.error('加载竞赛详情失败:', error)
      }
    }
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

// 清除竞赛筛选
const clearCompetitionFilter = () => {
  selectedCompetition.value = null
  currentCompetition.value = null
  router.push({ path: '/problems', query: {} })
  currentPage.value = 1
  loadProblems()
}

// 加载题目列表
const loadProblems = async () => {
  loading.value = true
  try {
    const page = currentPage.value - 1
    // 当有competitionId时，不使用搜索关键词，并优先使用路由中的competitionId
    const keyword = competitionIdFromRoute.value ? null : (searchKeyword.value || null)
    // 优先使用路由中的competitionId，否则使用selectedCompetition
    const competitionId = competitionIdFromRoute.value || selectedCompetition.value
    const data = await problemService.getChallenges(
      competitionId,
      page,
      pageSize.value,
      selectedCategory.value,
      selectedDifficulty.value,
      keyword
    )

    problems.value = (data.challenges || []).map(p => ({
      id: p.challengeID || p.id || p.challengeId,
      title: p.title || p.Title || '',
      description: p.description || p.Description || '',
      score: p.points || p.Points || 0,
      category: p.category || p.Category || '未分类',
      difficulty: p.difficulty || p.Difficulty || 'Easy',
      solved: p.solved || false,
      solveCount: p.solveCount || 0
    }))

    // 提取所有分类
    const allCategories = problems.value
      .map(p => p.category)
      .filter(cat => cat && cat !== '未分类' && cat.trim() !== '')
    categories.value = [...new Set(allCategories)].sort()

    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (error) {
    console.error('加载题目列表失败:', error)
    MessagePlugin.error('加载题目列表失败: ' + (error.message || '未知错误'))
    problems.value = []
    categories.value = []
  } finally {
    loading.value = false
  }
}

// 切换分类
const toggleCategory = (category) => {
  if (selectedCategory.value === category) {
    selectedCategory.value = null
  } else {
    selectedCategory.value = category
  }
  currentPage.value = 1
  loadProblems()
}

// 分页变化
const handlePageChange = (pageInfo) => {
  currentPage.value = pageInfo.current
  loadProblems()
}

// 提交Flag
const handleFlagSubmit = async (problemId, flag) => {
  if (!isCompetitionOngoing.value) {
    showWarning('竞赛未开始或已结束，无法提交Flag')
    return
  }
  
  try {
    // 确保使用正确的problemId
    const actualProblemId = problemId || problems.value.find(p => p.id === problemId || p.challengeID === problemId)?.id || problemId
    const result = await problemService.submitFlag(actualProblemId, flag)
    if (result.isCorrect) {
      // 更新问题状态
      const problem = problems.value.find(p => (p.id === problemId) || (p.challengeID === problemId))
      if (problem) {
        problem.solved = true
      }
      showSuccess(`Flag正确！获得 ${problem?.score || 0} 分`)
      // 重新加载列表以更新解决状态
      loadProblems()
      // 重新加载队伍总分
      if (competitionIdFromRoute.value) {
        await loadTeamInfo()
      }
    } else {
      showError(result.message || 'Flag错误，请重试')
    }
  } catch (error) {
    logError('提交Flag失败:', error)
    // 从错误响应中提取消息，如果是业务错误（400），显示具体原因
    const errorMessage = error.response?.data?.message || error.message || '提交失败'
    // 只显示业务错误信息，系统错误显示通用提示
    if (error.response?.status === 400) {
      showError(errorMessage)
    } else {
      showError('提交失败，请稍后重试')
    }
  }
}

// 查看提示
const handleViewHints = async (problemId) => {
  try {
    // 确保使用正确的problemId
    const actualProblemId = problemId || problems.value.find(p => p.id === problemId || p.challengeID === problemId)?.id || problemId
    const hints = await problemService.getHints(actualProblemId)
    if (hints && hints.length > 0) {
      currentHints.value = hints
      const problem = problems.value.find(p => (p.id === problemId) || (p.challengeID === problemId))
      currentProblemTitle.value = problem?.title || '题目提示'
      showHintsDialog.value = true
    } else {
      MessagePlugin.warning('该题目暂无提示')
    }
  } catch (error) {
    console.error('获取提示失败:', error)
    MessagePlugin.error('获取提示失败: ' + (error.message || '未知错误'))
  }
}

// 监听路由参数变化
watch(() => route.query.competitionId, (newId) => {
  if (newId) {
    const numId = parseInt(newId, 10)
    if (!isNaN(numId)) {
      selectedCompetition.value = numId
      loadCompetitions()
      loadProblems()
    }
  } else {
    currentTeam.value = null
    teamTotalScore.value = null
  }
}, { immediate: true })

onMounted(() => {
  loadCompetitions()
  // 如果有竞赛ID，等待loadCompetitions完成后再加载题目
  setTimeout(() => {
    loadProblems()
  }, 100)
})
</script>

<style scoped>
.problems-page {
  animation: fadeIn 0.3s ease-in-out;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #e0e6ed;
  margin: 0;
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

.competition-info {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.category-tags {
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, 0.1);
}

.category-tags-header {
  margin-bottom: 12px;
}

.category-label {
  font-size: 14px;
  font-weight: 500;
  color: #9ca3af;
}

.category-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tag {
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 6px 12px;
  font-size: 13px;
}

.category-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.problems-list {
  min-height: 400px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.problems-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #2a3458;
}

.hints-list {
  max-height: 400px;
  overflow-y: auto;
}

.hint-item {
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 8px;
  border-left: 3px solid #3b82f6;
}

.hint-number {
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 8px;
}

.hint-content {
  color: #d1d5db;
  line-height: 1.6;
}

.empty-hints {
  text-align: center;
  color: #9ca3af;
  padding: 40px 0;
}

.team-info-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(96, 165, 250, 0.05) 100%);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.team-info-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.team-info-section {
  flex: 1;
  min-width: 300px;
}

.team-info-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.team-info-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-detail-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(42, 52, 88, 0.5);
}

.team-detail-item:last-child {
  border-bottom: none;
}

.team-detail-label {
  color: #9ca3af;
  font-size: 14px;
  min-width: 80px;
}

.team-detail-value {
  color: #e0e6ed;
  font-size: 14px;
  font-weight: 500;
}

.team-score-section {
  flex: 0 0 auto;
  min-width: 200px;
  text-align: center;
  padding: 20px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.team-score-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.team-score-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.score-number {
  font-size: 36px;
  font-weight: 700;
  color: #fbbf24;
  line-height: 1;
}

.score-unit {
  font-size: 18px;
  color: #9ca3af;
  margin-left: 4px;
}

.team-info-empty {
  width: 100%;
  padding: 20px 0;
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

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    flex-direction: column;
    width: 100%;
  }

  .filter-actions > * {
    width: 100%;
  }

  .problems-grid {
    grid-template-columns: 1fr;
  }
}
</style>

