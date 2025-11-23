<template>
  <div class="competition-audit-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">竞赛审核</h1>
        <p class="page-subtitle">审核待审核的竞赛申请</p>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-select
          v-model="auditFilter"
          placeholder="筛选审核状态"
          style="width: 200px;"
          clearable
          @change="loadCompetitions"
        >
          <t-option value="PENDING" label="待审核" />
          <t-option value="APPROVED" label="已通过" />
          <t-option value="REJECTED" label="已拒绝" />
        </t-select>
        <t-button @click="loadCompetitions">刷新</t-button>
      </div>
      <t-table
        :data="competitions"
        :columns="competitionColumns"
        :loading="loadingCompetitions"
        row-key="competitionID"
        :pagination="competitionPagination"
        @page-change="handleCompetitionPageChange"
      >
        <template #status="{ row }">
          <t-tag :theme="getStatusTheme(row.status)">
            {{ getStatusLabel(row.status) }}
          </t-tag>
        </template>
        <template #auditStatus="{ row }">
          <t-tag :theme="getAuditTheme(row.auditStatus)">
            {{ getAuditLabel(row.auditStatus) }}
          </t-tag>
        </template>
        <template #actions="{ row }">
          <t-button 
            v-if="row.auditStatus === 'PENDING' || !row.auditStatus"
            variant="text" 
            size="small" 
            theme="success"
            @click="auditCompetition(row, true)"
          >
            通过
          </t-button>
          <t-button 
            v-if="row.auditStatus === 'PENDING' || !row.auditStatus"
            variant="text" 
            size="small" 
            theme="warning"
            @click="showRejectCompetitionDialogFunc(row)"
          >
            拒绝
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 拒绝竞赛对话框 -->
    <t-dialog
      v-model:visible="showRejectCompetitionDialog"
      header="拒绝竞赛"
      width="500px"
      :footer="false"
    >
      <t-form :data="rejectCompetitionForm" @submit="handleRejectCompetition">
        <t-form-item label="拒绝原因" name="auditRemark">
          <t-textarea v-model="rejectCompetitionForm.auditRemark" placeholder="请输入拒绝原因" :rows="3" />
        </t-form-item>
        <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
          <t-button variant="outline" @click="showRejectCompetitionDialog = false">取消</t-button>
          <t-button theme="primary" type="submit">确认拒绝</t-button>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminService } from '@/services/adminService'
import { MessagePlugin } from 'tdesign-vue-next'
import { showSuccess, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

// 竞赛审核
const competitions = ref([])
const loadingCompetitions = ref(false)
const auditFilter = ref('PENDING') // 默认显示待审核
const competitionPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const competitionColumns = [
  { colKey: 'competitionID', title: 'ID', width: 80 },
  { colKey: 'title', title: '标题', width: 200 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'auditStatus', title: '审核状态', width: 120 },
  { colKey: 'startTime', title: '开始时间', width: 180 },
  { colKey: 'endTime', title: '结束时间', width: 180 },
  { colKey: 'actions', title: '操作', width: 150, fixed: 'right' }
]

// 审核竞赛相关
const showRejectCompetitionDialog = ref(false)
const rejectingCompetition = ref(null)
const rejectCompetitionForm = ref({
  auditRemark: ''
})

// 加载竞赛列表
const loadCompetitions = async () => {
  loadingCompetitions.value = true
  try {
    const data = await adminService.getCompetitions(
      competitionPagination.value.current - 1,
      competitionPagination.value.pageSize
    )
    // 根据筛选条件过滤
    let filteredCompetitions = data.competitions || []
    if (auditFilter.value) {
      filteredCompetitions = filteredCompetitions.filter(comp => {
        const auditStatus = comp.auditStatus || comp.AuditStatus || 'PENDING'
        return auditStatus === auditFilter.value
      })
    }
    competitions.value = filteredCompetitions
    competitionPagination.value.total = filteredCompetitions.length
  } catch (error) {
    logError('加载竞赛列表失败:', error)
    handleApiError(error, '加载竞赛列表失败')
  } finally {
    loadingCompetitions.value = false
  }
}

// 竞赛分页
const handleCompetitionPageChange = (pageInfo) => {
  competitionPagination.value.current = pageInfo.current
  loadCompetitions()
}

// 审核竞赛
const auditCompetition = async (competition, approved, auditRemark = '') => {
  try {
    await adminService.auditCompetition(
      competition.competitionID || competition.id,
      approved,
      auditRemark
    )
    showSuccess('审核成功')
    loadCompetitions()
  } catch (error) {
    MessagePlugin.error('审核失败: ' + (error.message || '未知错误'))
  }
}

// 显示拒绝竞赛对话框
const showRejectCompetitionDialogFunc = (competition) => {
  rejectingCompetition.value = competition
  rejectCompetitionForm.value.auditRemark = ''
  showRejectCompetitionDialog.value = true
}

// 处理拒绝竞赛
const handleRejectCompetition = async () => {
  if (!rejectingCompetition.value) return
  try {
    await auditCompetition(
      rejectingCompetition.value,
      false,
      rejectCompetitionForm.value.auditRemark
    )
    showRejectCompetitionDialog.value = false
    rejectingCompetition.value = null
    rejectCompetitionForm.value.auditRemark = ''
  } catch (error) {
    // 错误已在 auditCompetition 中处理
  }
}

// 获取状态主题
const getStatusTheme = (status) => {
  const s = (status || '').toLowerCase()
  if (s === 'active' || s === 'ongoing') return 'success'
  if (s === 'upcoming' || s === 'published') return 'warning'
  if (s === 'ended' || s === 'finished') return 'default'
  return 'default'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const s = (status || '').toLowerCase()
  if (s === 'active' || s === 'ongoing') return '进行中'
  if (s === 'upcoming' || s === 'published') return '即将开始'
  if (s === 'ended' || s === 'finished') return '已结束'
  return '未知'
}

// 获取审核状态主题
const getAuditTheme = (auditState) => {
  const s = String(auditState || '')
  if (s === '1' || s === 'APPROVED') return 'success'
  if (s === '2' || s === 'REJECTED') return 'danger'
  return 'warning'
}

// 获取审核状态标签
const getAuditLabel = (auditState) => {
  const s = String(auditState || '')
  if (s === '1' || s === 'APPROVED') return '已通过'
  if (s === '2' || s === 'REJECTED') return '已拒绝'
  return '待审核'
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.competition-audit-page {
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

