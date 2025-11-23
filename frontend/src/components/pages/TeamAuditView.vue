<template>
  <div class="team-audit-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">团队审核</h1>
        <p class="page-subtitle">审核待审核的团队申请</p>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-select
          v-model="teamAuditFilter"
          placeholder="筛选审核状态"
          style="width: 200px;"
          clearable
          @change="loadTeams"
        >
          <t-option value="0" label="待审核" />
          <t-option value="1" label="已通过" />
          <t-option value="2" label="已拒绝" />
        </t-select>
        <t-button @click="loadTeams">刷新</t-button>
      </div>
      <t-table
        :data="teams"
        :columns="teamColumns"
        :loading="loadingTeams"
        row-key="teamID"
        :pagination="teamPagination"
        @page-change="handleTeamPageChange"
      >
        <template #auditState="{ row }">
          <t-tag :theme="getAuditTheme(row.auditState)">
            {{ getAuditLabel(row.auditState) }}
          </t-tag>
        </template>
        <template #actions="{ row }">
          <t-button 
            v-if="row.auditState === '0'"
            variant="text" 
            size="small" 
            theme="success"
            @click="auditTeam(row, '1', '')"
          >
            通过
          </t-button>
          <t-button 
            v-if="row.auditState === '0'"
            variant="text" 
            size="small" 
            theme="danger"
            @click="showRejectDialog(row)"
          >
            拒绝
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 拒绝团队对话框 -->
    <t-dialog
      v-model:visible="showRejectTeamDialog"
      header="拒绝团队"
      width="400px"
      :footer="false"
    >
      <t-form :data="rejectTeamForm" @submit="handleRejectTeam">
        <t-form-item label="拒绝原因" name="auditRemark">
          <t-textarea v-model="rejectTeamForm.auditRemark" placeholder="请输入拒绝原因" :rows="3" />
        </t-form-item>
        <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
          <t-button variant="outline" @click="showRejectTeamDialog = false">取消</t-button>
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

// 团队审核
const teams = ref([])
const loadingTeams = ref(false)
const teamAuditFilter = ref('0') // 默认显示待审核
const teamPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const teamColumns = [
  { colKey: 'teamID', title: 'ID', width: 80 },
  { colKey: 'teamName', title: '团队名称', width: 200 },
  { colKey: 'competitionID', title: '竞赛ID', width: 100 },
  { colKey: 'auditState', title: '审核状态', width: 120 },
  { colKey: 'creationTime', title: '创建时间', width: 180 },
  { colKey: 'actions', title: '操作', width: 150, fixed: 'right' }
]
const showRejectTeamDialog = ref(false)
const rejectingTeam = ref(null)
const rejectTeamForm = ref({
  auditRemark: ''
})

// 加载团队列表
const loadTeams = async () => {
  loadingTeams.value = true
  try {
    const data = await adminService.getTeams(
      teamPagination.value.current - 1,
      teamPagination.value.pageSize,
      null,
      teamAuditFilter.value || null
    )
    teams.value = data.teams || []
    teamPagination.value.total = data.totalElements || 0
  } catch (error) {
    logError('加载团队列表失败:', error)
    handleApiError(error, '加载团队列表失败')
  } finally {
    loadingTeams.value = false
  }
}

// 团队分页
const handleTeamPageChange = (pageInfo) => {
  teamPagination.value.current = pageInfo.current
  loadTeams()
}

// 审核团队
const auditTeam = async (team, auditState, auditRemark = '') => {
  try {
    await adminService.auditTeam(team.teamID || team.id, auditState, auditRemark)
    MessagePlugin.success('审核成功')
    loadTeams()
  } catch (error) {
    MessagePlugin.error('审核失败: ' + (error.message || '未知错误'))
  }
}

// 显示拒绝对话框
const showRejectDialog = (team) => {
  rejectingTeam.value = team
  rejectTeamForm.value.auditRemark = ''
  showRejectTeamDialog.value = true
}

// 处理拒绝团队
const handleRejectTeam = async () => {
  if (!rejectingTeam.value) return
  try {
    await auditTeam(rejectingTeam.value, '2', rejectTeamForm.value.auditRemark)
    showRejectTeamDialog.value = false
    rejectingTeam.value = null
  } catch (error) {
    // 错误已在 auditTeam 中处理
  }
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
  loadTeams()
})
</script>

<style scoped>
.team-audit-page {
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

