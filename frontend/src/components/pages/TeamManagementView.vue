<template>
  <div class="team-management-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">团队管理</h1>
        <p class="page-subtitle">查看和管理所有团队</p>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-input
          v-model="searchKeyword"
          placeholder="搜索团队..."
          clearable
          style="width: 300px;"
          @enter="loadTeams"
        >
          <template #prefix-icon>
            <t-icon name="search" />
          </template>
        </t-input>
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
            variant="text" 
            size="small" 
            theme="danger"
            @click="deleteTeam(row)"
          >
            删除
          </t-button>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminService } from '@/services/adminService'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { showSuccess, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

// 团队管理
const teams = ref([])
const loadingTeams = ref(false)
const searchKeyword = ref('')
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

// 加载团队列表
const loadTeams = async () => {
  loadingTeams.value = true
  try {
    const data = await adminService.getTeams(
      teamPagination.value.current - 1,
      teamPagination.value.pageSize,
      searchKeyword.value || null,
      null // 不筛选审核状态，显示所有团队
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

// 删除团队
const deleteTeam = async (team) => {
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除团队 "${team.teamName || team.name}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      try {
        await adminService.deleteTeam(team.teamID || team.id)
        showSuccess('删除成功')
        loadTeams()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    }
  })
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
.team-management-page {
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

