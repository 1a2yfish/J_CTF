<template>
  <div class="challenge-edit-page">
    <div class="page-header">
      <div class="header-content">
        <div style="display: flex; align-items: center; gap: 16px;">
          <t-button variant="text" @click="goBack">
            <t-icon name="chevron-left" /> 返回
          </t-button>
          <div>
            <h1 class="page-title">题目编辑</h1>
            <p class="page-subtitle" v-if="competitionInfo">
              {{ competitionInfo.title || competitionInfo.Title || '竞赛题目管理' }}
            </p>
            <p class="page-subtitle" v-else>管理竞赛题目</p>
          </div>
        </div>
      </div>
    </div>

    <div class="tab-content">
      <div class="table-header">
        <t-button 
          theme="primary" 
          @click="handleCreateChallengeClick"
        >
          <t-icon name="add" /> 创建题目
        </t-button>
        <t-button @click="loadChallenges">刷新</t-button>
      </div>
      <t-table
        :data="challenges"
        :columns="challengeColumns"
        :loading="loadingChallenges"
        row-key="challengeID"
        :pagination="challengePagination"
        @page-change="handleChallengePageChange"
      >
        <template #difficulty="{ row }">
          <t-tag :theme="getDifficultyTheme(row.difficulty)">
            {{ row.difficulty }}
          </t-tag>
        </template>
        <template #actions="{ row }">
          <t-button variant="text" size="small" @click="editChallenge(row)">编辑</t-button>
          <t-button 
            variant="text" 
            size="small" 
            theme="danger"
            @click="deleteChallenge(row)"
          >
            删除
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 创建/编辑题目对话框 -->
    <t-dialog
      v-model:visible="showCreateChallenge"
      :header="editingChallenge ? '编辑题目' : '创建题目'"
      width="700px"
      :footer="false"
    >
      <t-form :data="challengeForm" @submit="handleChallengeSubmit">
        <t-form-item label="题目标题" name="title" :rules="[{ required: true, message: '请输入题目标题' }]">
          <t-input v-model="challengeForm.title" placeholder="请输入题目标题" />
        </t-form-item>
        <t-form-item label="题目描述" name="description" :rules="[{ required: true, message: '请输入题目描述' }]">
          <t-textarea v-model="challengeForm.description" placeholder="请输入题目描述" :rows="5" />
        </t-form-item>
        <t-form-item label="分类" name="category" :rules="[{ required: true, message: '请选择分类' }]">
          <t-select v-model="challengeForm.category" placeholder="请选择分类">
            <t-option value="Web" label="Web" />
            <t-option value="Pwn" label="Pwn" />
            <t-option value="Crypto" label="Crypto" />
            <t-option value="Reverse" label="Reverse" />
            <t-option value="Misc" label="Misc" />
          </t-select>
        </t-form-item>
        <t-form-item label="难度" name="difficulty" :rules="[{ required: true, message: '请选择难度' }]">
          <t-select v-model="challengeForm.difficulty" placeholder="请选择难度">
            <t-option value="Easy" label="简单" />
            <t-option value="Medium" label="中等" />
            <t-option value="Hard" label="困难" />
          </t-select>
        </t-form-item>
        <t-form-item label="分值" name="points" :rules="[{ required: true, message: '请输入分值' }]">
          <t-input-number v-model="challengeForm.points" :min="1" />
        </t-form-item>
        <t-form-item label="Flag" name="flag" :rules="[{ required: true, message: '请输入Flag' }]">
          <t-input v-model="challengeForm.flag" placeholder="请输入Flag" type="password" />
        </t-form-item>
        <t-form-item label="提示" name="hint">
          <t-textarea v-model="challengeForm.hint" placeholder="请输入提示（可选）" :rows="3" />
        </t-form-item>
        <t-form-item label="附件链接" name="attachmentUrl">
          <t-input v-model="challengeForm.attachmentUrl" placeholder="请输入附件下载链接（可选）" />
        </t-form-item>
        <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
          <t-button variant="outline" @click="showCreateChallenge = false">取消</t-button>
          <t-button theme="primary" type="submit">保存</t-button>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminService } from '@/services/adminService'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { showSuccess, showError, showWarning, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const router = useRouter()
const route = useRoute()

// 从路由参数获取竞赛ID
const competitionId = computed(() => {
  return route.params.id ? Number(route.params.id) : null
})

// 竞赛信息
const competitionInfo = ref(null)

// 题目管理
const challenges = ref([])
const loadingChallenges = ref(false)
const challengePagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const challengeColumns = [
  { colKey: 'challengeID', title: 'ID', width: 80 },
  { colKey: 'title', title: '题目标题', width: 200 },
  { colKey: 'category', title: '分类', width: 100 },
  { colKey: 'difficulty', title: '难度', width: 100 },
  { colKey: 'points', title: '分值', width: 80 },
  { colKey: 'solveCount', title: '解题数', width: 100 },
  { colKey: 'actions', title: '操作', width: 150, fixed: 'right' }
]
const showCreateChallenge = ref(false)
const editingChallenge = ref(null)
const challengeForm = ref({
  competitionID: null,
  title: '',
  description: '',
  category: '',
  difficulty: '',
  points: 100,
  flag: '',
  hint: '',
  attachmentUrl: ''
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载竞赛信息
const loadCompetitionInfo = async () => {
  if (!competitionId.value) return
  try {
    const data = await adminService.getCompetitionDetails(competitionId.value)
    competitionInfo.value = data
  } catch (error) {
    logError('加载竞赛信息失败:', error)
  }
}

// 加载题目列表
const loadChallenges = async () => {
  if (!competitionId.value) {
    MessagePlugin.error('缺少竞赛ID参数')
    return
  }
  
  loadingChallenges.value = true
  try {
    const params = {
      page: challengePagination.value.current - 1,
      size: challengePagination.value.pageSize,
      competitionId: competitionId.value
    }
    const response = await adminService.getChallenges(params)
    challenges.value = response.challenges || []
    challengePagination.value.total = response.totalElements || 0
  } catch (error) {
    logError('加载题目列表失败:', error)
    handleApiError(error, '加载题目列表失败')
  } finally {
    loadingChallenges.value = false
  }
}

// 题目分页
const handleChallengePageChange = (pageInfo) => {
  challengePagination.value.current = pageInfo.current
  loadChallenges()
}

// 创建题目按钮点击处理
const handleCreateChallengeClick = () => {
  if (!competitionId.value) {
    MessagePlugin.error('缺少竞赛ID')
    return
  }
  
  challengeForm.value.competitionID = competitionId.value
  editingChallenge.value = null
  challengeForm.value = {
    competitionID: competitionId.value,
    title: '',
    description: '',
    category: '',
    difficulty: '',
    points: 100,
    flag: '',
    hint: '',
    attachmentUrl: ''
  }
  showCreateChallenge.value = true
}

// 编辑题目
const editChallenge = async (challenge) => {
  try {
    const details = await adminService.getChallengeDetails(challenge.challengeID || challenge.id)
    editingChallenge.value = details
    
    let competitionIdValue = competitionId.value
    if (!competitionIdValue) {
      if (details.competition) {
        competitionIdValue = details.competition.competitionID || details.competition.id || details.competition.competitionId
      }
      if (!competitionIdValue) {
        competitionIdValue = details.competitionID || details.competitionId
      }
    }
    
    challengeForm.value = {
      competitionID: competitionIdValue,
      title: details.title || '',
      description: details.description || '',
      category: details.category || '',
      difficulty: details.difficulty || '',
      points: details.points || 100,
      flag: details.flag || '',
      hint: details.hint || '',
      attachmentUrl: details.attachmentUrl || ''
    }
    
    showCreateChallenge.value = true
  } catch (error) {
    console.error('获取题目详情失败:', error)
    MessagePlugin.error('获取题目详情失败: ' + (error.message || '未知错误'))
  }
}

// 删除题目
const deleteChallenge = async (challenge) => {
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除题目 "${challenge.title}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      try {
        await adminService.deleteChallenge(challenge.challengeID || challenge.id)
        showSuccess('删除成功')
        loadChallenges()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 提交题目表单
const handleChallengeSubmit = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    showWarning(firstError)
    return
  }
  
  try {
    if (editingChallenge.value) {
      const updateData = { ...challengeForm.value }
      delete updateData.competitionID
      
      await adminService.updateChallenge(
        editingChallenge.value.challengeID || editingChallenge.value.id,
        updateData
      )
      showSuccess('更新成功')
    } else {
      if (!challengeForm.value.competitionID) {
        showError('请选择竞赛')
        return
      }
      
      const submitData = {
        ...challengeForm.value,
        competitionID: Number(challengeForm.value.competitionID) || challengeForm.value.competitionID
      }
      
      await adminService.createChallenge(submitData)
      showSuccess('创建成功')
    }
    showCreateChallenge.value = false
    editingChallenge.value = null
    challengeForm.value = {
      competitionID: competitionId.value,
      title: '',
      description: '',
      category: '',
      difficulty: '',
      points: 100,
      flag: '',
      hint: '',
      attachmentUrl: ''
    }
    loadChallenges()
  } catch (error) {
    console.error('提交题目失败:', error)
    MessagePlugin.error('操作失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 获取难度主题
const getDifficultyTheme = (difficulty) => {
  const d = (difficulty || '').toLowerCase()
  if (d === 'easy') return 'success'
  if (d === 'medium') return 'warning'
  if (d === 'hard') return 'danger'
  return 'default'
}

onMounted(() => {
  if (competitionId.value) {
    loadCompetitionInfo()
    loadChallenges()
  } else {
    MessagePlugin.error('缺少竞赛ID参数')
  }
})
</script>

<style scoped>
.challenge-edit-page {
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

