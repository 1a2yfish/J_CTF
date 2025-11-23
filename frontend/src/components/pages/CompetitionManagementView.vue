<template>
  <div class="competition-management-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">竞赛管理</h1>
        <p class="page-subtitle">创建、编辑和管理竞赛</p>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-button theme="primary" @click="showCreateCompetition = true">
          <t-icon name="add" /> 创建竞赛
        </t-button>
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
          <t-button variant="text" size="small" @click="editCompetition(row)">编辑</t-button>
          <t-button 
            variant="text" 
            size="small" 
            theme="primary"
            @click="goToChallengeEdit(row)"
          >
            编辑题目
          </t-button>
          <t-button 
            variant="text" 
            size="small" 
            theme="danger"
            @click="deleteCompetition(row)"
          >
            删除
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 创建/编辑竞赛对话框 -->
    <t-dialog
      v-model:visible="showCreateCompetition"
      :header="editingCompetition ? '编辑竞赛' : '创建竞赛'"
      width="600px"
      :footer="false"
    >
      <t-form :data="competitionForm" @submit="handleCompetitionSubmit">
        <t-form-item label="竞赛标题" name="title" :rules="[{ required: true, message: '请输入竞赛标题' }]">
          <t-input v-model="competitionForm.title" placeholder="请输入竞赛标题" />
        </t-form-item>
        <t-form-item label="竞赛介绍" name="introduction" :rules="[{ required: true, message: '请输入竞赛介绍' }]">
          <t-textarea v-model="competitionForm.introduction" placeholder="请输入竞赛介绍" :rows="4" />
        </t-form-item>
        <t-form-item label="开始时间" name="startTime" :rules="[{ required: true, message: '请选择开始时间' }]">
          <t-date-picker v-model="competitionForm.startTime" enable-time-picker />
        </t-form-item>
        <t-form-item label="结束时间" name="endTime" :rules="[{ required: true, message: '请选择结束时间' }]">
          <t-date-picker v-model="competitionForm.endTime" enable-time-picker />
        </t-form-item>
        <t-form-item label="团队人数限制" name="teamSizeLimit" :rules="[{ required: true, message: '请输入团队人数限制' }]">
          <t-input-number v-model="competitionForm.teamSizeLimit" :min="1" :max="20" />
        </t-form-item>
        <t-form-item label="最大团队数" name="maxTeams">
          <t-input-number v-model="competitionForm.maxTeams" :min="1" />
        </t-form-item>
        <t-form-item label="是否公开" name="isPublic">
          <t-switch v-model="competitionForm.isPublic" />
        </t-form-item>
        <t-form-item v-if="editingCompetition" label="状态" name="status">
          <t-select v-model="competitionForm.status" placeholder="请选择状态">
            <t-option value="DRAFT" label="草稿" />
            <t-option value="PUBLISHED" label="已发布" />
            <!-- <t-option value="ONGOING" label="进行中" />
            <t-option value="FINISHED" label="已结束" />
            <t-option value="CANCELLED" label="已取消" /> -->
          </t-select>
        </t-form-item>
        <t-form-item v-if="editingCompetition" label="审核状态" name="auditStatus">
          <t-select v-model="competitionForm.auditStatus" placeholder="请选择审核状态">
            <t-option value="PENDING" label="待审核" />
            <t-option value="APPROVED" label="已通过" />
            <t-option value="REJECTED" label="已拒绝" />
          </t-select>
        </t-form-item>
        <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
          <t-button variant="outline" @click="showCreateCompetition = false">取消</t-button>
          <t-button theme="primary" type="submit">保存</t-button>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminService } from '@/services/adminService'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { showSuccess, showError, showWarning, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const router = useRouter()

// 竞赛管理
const competitions = ref([])
const loadingCompetitions = ref(false)
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
  { colKey: 'actions', title: '操作', width: 200, fixed: 'right' }
]
const showCreateCompetition = ref(false)
const editingCompetition = ref(null)
const competitionForm = ref({
  title: '',
  introduction: '',
  startTime: '',
  endTime: '',
  teamSizeLimit: 5,
  maxTeams: null,
  isPublic: true,
  status: 'DRAFT',
  auditStatus: 'PENDING'
})

// 加载竞赛列表
const loadCompetitions = async () => {
  loadingCompetitions.value = true
  try {
    const data = await adminService.getCompetitions(
      competitionPagination.value.current - 1,
      competitionPagination.value.pageSize
    )
    competitions.value = data.competitions || []
    competitionPagination.value.total = data.totalElements || 0
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

// 编辑竞赛
const editCompetition = async (competition) => {
  try {
    const competitionId = competition.competitionID || competition.id
    const competitionDetails = await adminService.getCompetitionDetails(competitionId)
    
    editingCompetition.value = competitionDetails
    competitionForm.value = {
      title: competitionDetails.title || competitionDetails.Title || '',
      introduction: competitionDetails.introduction || competitionDetails.Introduction || '',
      startTime: competitionDetails.startTime || competitionDetails.StartTime || '',
      endTime: competitionDetails.endTime || competitionDetails.EndTime || '',
      teamSizeLimit: competitionDetails.teamSizeLimit || competitionDetails.teamSizeLimit || 5,
      maxTeams: competitionDetails.maxTeams || competitionDetails.maxTeams || null,
      isPublic: competitionDetails.isPublic !== false && competitionDetails.isPublic !== null,
      status: competitionDetails.status || competitionDetails.Status || 'DRAFT',
      auditStatus: competitionDetails.auditStatus || competitionDetails.AuditStatus || 'PENDING'
    }
    showCreateCompetition.value = true
  } catch (error) {
    console.error('获取竞赛详情失败:', error)
    MessagePlugin.error('获取竞赛详情失败: ' + (error.message || '未知错误'))
  }
}

// 跳转到题目编辑页面
const goToChallengeEdit = (competition) => {
  const competitionId = competition.competitionID || competition.id
  router.push(`/admin/challenges/${competitionId}`)
}

// 删除竞赛
const deleteCompetition = async (competition) => {
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除竞赛 "${competition.title || competition.Title}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      try {
        await adminService.deleteCompetition(competition.competitionID || competition.id)
        showSuccess('删除成功')
        loadCompetitions()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 提交竞赛表单
const handleCompetitionSubmit = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    showWarning(firstError)
    return
  }
  
  try {
    const submitData = { ...competitionForm.value }
    
    if (submitData.startTime) {
      if (submitData.startTime instanceof Date) {
        submitData.startTime = submitData.startTime.toISOString()
      } else if (typeof submitData.startTime === 'string') {
        submitData.startTime = new Date(submitData.startTime).toISOString()
      }
    }
    if (submitData.endTime) {
      if (submitData.endTime instanceof Date) {
        submitData.endTime = submitData.endTime.toISOString()
      } else if (typeof submitData.endTime === 'string') {
        submitData.endTime = new Date(submitData.endTime).toISOString()
      }
    }
    
    if (editingCompetition.value) {
      const competitionId = editingCompetition.value.competitionID || editingCompetition.value.id
      await adminService.updateCompetition(competitionId, submitData)
      showSuccess('更新成功')
    } else {
      await adminService.createCompetition(submitData)
      showSuccess('创建成功')
    }
    showCreateCompetition.value = false
    editingCompetition.value = null
    competitionForm.value = {
      title: '',
      introduction: '',
      startTime: '',
      endTime: '',
      teamSizeLimit: 5,
      maxTeams: null,
      isPublic: true,
      status: 'DRAFT',
      auditStatus: 'PENDING'
    }
    loadCompetitions()
  } catch (error) {
    MessagePlugin.error('操作失败: ' + (error.message || '未知错误'))
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
.competition-management-page {
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

