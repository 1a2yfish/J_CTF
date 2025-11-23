<template>
  <div class="flag-submission-page">
    <div class="page-header">
      <div class="header-content">
        <div style="display: flex; align-items: center; gap: 16px;">
          <t-button variant="text" @click="goBack">
            <t-icon name="chevron-left" /> 返回
          </t-button>
          <div>
            <h1 class="page-title">Flag提交</h1>
            <p class="page-subtitle">查看和管理Flag提交记录</p>
          </div>
        </div>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-select
          v-model="submissionCompetitionFilter"
          placeholder="筛选竞赛"
          style="width: 200px;"
          clearable
          @change="loadFlagSubmissions"
        >
          <t-option
            v-for="comp in competitions"
            :key="comp.competitionID || comp.id"
            :value="comp.competitionID || comp.id"
            :label="comp.title || comp.Title"
          />
        </t-select>
        <t-button @click="loadFlagSubmissions">刷新</t-button>
      </div>
      <t-table
        :data="flagSubmissions"
        :columns="submissionColumns"
        :loading="loadingSubmissions"
        row-key="submissionID"
        :pagination="submissionPagination"
        @page-change="handleSubmissionPageChange"
      >
        <template #isCorrect="{ row }">
          <t-tag :theme="row.isCorrect ? 'success' : 'danger'">
            {{ row.isCorrect ? '正确' : '错误' }}
          </t-tag>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminService } from '@/services/adminService'
import { handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const router = useRouter()

// 竞赛列表（用于筛选）
const competitions = ref([])

// Flag提交
const flagSubmissions = ref([])
const loadingSubmissions = ref(false)
const submissionCompetitionFilter = ref(null)
const submissionPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const submissionColumns = [
  { colKey: 'submissionID', title: 'ID', width: 80 },
  { colKey: 'userName', title: '用户', width: 150 },
  { colKey: 'challengeTitle', title: '题目', width: 200 },
  { colKey: 'submittedContent', title: '提交内容', width: 200 },
  { colKey: 'isCorrect', title: '结果', width: 100 },
  { colKey: 'submitTime', title: '提交时间', width: 180 }
]

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载竞赛列表用于筛选
const loadCompetitionsForFilter = async () => {
  try {
    const data = await adminService.getCompetitions(0, 100)
    competitions.value = data.competitions || []
  } catch (error) {
    logError('加载竞赛列表失败:', error)
  }
}

// 加载Flag提交列表
const loadFlagSubmissions = async () => {
  loadingSubmissions.value = true
  try {
    const params = {
      page: submissionPagination.value.current - 1,
      size: submissionPagination.value.pageSize
    }
    if (submissionCompetitionFilter.value) {
      params.competitionId = submissionCompetitionFilter.value
    }
    const response = await adminService.getFlagSubmissions(params)
    const submissions = (response.submissions || []).map(sub => ({
      ...sub,
      userName: sub.user?.userName || sub.userName || '未知用户',
      userEmail: sub.user?.userEmail || sub.userEmail || '',
      challengeTitle: sub.challenge?.title || sub.challengeTitle || '未知题目',
      challengeID: sub.challenge?.challengeID || sub.challengeID || null,
      teamName: sub.team?.teamName || sub.teamName || '',
      competitionTitle: sub.competition?.title || sub.competitionTitle || ''
    }))
    flagSubmissions.value = submissions
    submissionPagination.value.total = response.totalElements || 0
  } catch (error) {
    logError('加载Flag提交列表失败:', error)
    handleApiError(error, '加载Flag提交列表失败')
  } finally {
    loadingSubmissions.value = false
  }
}

// Flag提交分页
const handleSubmissionPageChange = (pageInfo) => {
  submissionPagination.value.current = pageInfo.current
  loadFlagSubmissions()
}

onMounted(() => {
  loadCompetitionsForFilter()
  loadFlagSubmissions()
})
</script>

<style scoped>
.flag-submission-page {
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

.tab-content {
  padding: 24px 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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

