<template>
  <div class="teams-page">
    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; position: relative; z-index: 10;">
        <div style="flex: 1;"></div>
        <div style="display: flex; gap: 12px; position: relative; z-index: 10;">
          <t-button 
            theme="primary" 
            @click="handleCreateTeamClick"
            :disabled="loading"
            style="pointer-events: auto; cursor: pointer;"
          >
            <t-icon name="add" /> 创建团队
          </t-button>
          <t-button 
            @click="handleJoinTeamClick"
            :disabled="loading"
            style="pointer-events: auto; cursor: pointer;"
          >
            <t-icon name="user-add" /> 加入团队
          </t-button>
        </div>
      </div>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <t-loading size="large" text="加载中..." />
      </div>
      <div v-else-if="error" style="text-align: center; padding: 40px; color: #f5222d;">
        <t-icon name="error-circle" size="48" />
        <div style="margin-top: 16px;">{{ error }}</div>
      </div>
      <div v-else-if="teams.length === 0" style="text-align: center; padding: 40px;">
        <t-empty description="您还没有加入任何团队" />
      </div>
      <div v-else>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px;">
          <TeamCard
              v-for="team in teams"
              :key="team.teamID || team.id"
              :team="team"
              :is-current-team="true"
              @view-members="handleViewMembers"
              @edit-description="handleEditDescription"
              @disband-team="handleDisbandTeam"
              @submit-writeup="handleSubmitWriteUp"
              @view-writeup="handleViewWriteUp"
          />
        </div>
      </div>
    </div>

    <!-- 创建团队对话框 -->
    <t-dialog
        header="创建团队"
        :visible="showCreateTeamModal"
        @close="showCreateTeamModal = false"
        :footer="false"
        width="500px"
    >
      <t-form :data="createTeamForm" label-width="90px" @submit="handleCreateTeam">
        <t-form-item label="竞赛" name="competitionId" :rules="[{ required: true, message: '请选择竞赛' }]">
          <t-select
              v-model="createTeamForm.competitionId"
              placeholder="请选择竞赛"
              :options="competitionOptions"
              :loading="loadingCompetitions"
          />
        </t-form-item>
        <t-form-item label="团队名称" name="teamName" :rules="[{ required: true, message: '请输入团队名称' }]">
          <t-input v-model="createTeamForm.teamName" placeholder="请输入团队名称" />
        </t-form-item>
        <t-form-item label="邀请码" name="inviteCode">
          <t-input v-model="createTeamForm.inviteCode" placeholder="如果有邀请码请填写" />
        </t-form-item>
        <t-form-item label="团队描述" name="description">
          <t-textarea 
            v-model="createTeamForm.description" 
            placeholder="请输入团队描述（可选）" 
            :rows="4"
            :maxlength="500"
            show-word-limit
          />
        </t-form-item>
        <t-alert theme="info" style="margin: 15px 0;">
          <div style="line-height: 1.6;">
            <div>• 创建团队后，您可以邀请其他用户加入（最多5人）</div>
            <div style="margin-top: 4px;">• 每个竞赛中只能加入一个团队，请确保您尚未在该竞赛中创建或加入其他团队</div>
          </div>
        </t-alert>
        <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
          <t-button theme="default" style="margin-right: 10px;" @click="showCreateTeamModal = false">取消</t-button>
          <t-button theme="primary" type="submit" :loading="creatingTeam">创建团队</t-button>
        </div>
      </t-form>
    </t-dialog>

    <!-- 加入团队对话框 -->
    <t-dialog
        header="加入团队"
        :visible="showJoinTeamModal"
        @close="showJoinTeamModal = false"
        :footer="false"
        width="600px"
    >
      <t-alert theme="info" style="margin-bottom: 20px;">
        <div style="line-height: 1.6;">
          <div>• 每个竞赛中只能加入一个团队</div>
          <div style="margin-top: 4px;">• 如果您已在该竞赛中创建或加入了其他团队，将无法加入新团队</div>
        </div>
      </t-alert>
      <div style="margin-bottom: 20px;">
        <t-input
            v-model="searchTeamKeyword"
            placeholder="搜索团队名称..."
            clearable
            @enter="searchTeams"
            style="margin-bottom: 16px;"
        >
          <template #prefix-icon>
            <t-icon name="search" />
          </template>
        </t-input>
        <t-select
            v-model="joinTeamCompetitionId"
            placeholder="选择竞赛（可选）"
            :options="competitionOptions"
            clearable
            style="margin-bottom: 16px;"
        />
        <t-button theme="primary" @click="searchTeams" :loading="searchingTeams">搜索</t-button>
      </div>
      <div v-if="searchingTeams" style="text-align: center; padding: 40px;">
        <t-loading size="large" text="搜索中..." />
      </div>
      <div v-else-if="availableTeams.length === 0" style="text-align: center; padding: 40px;">
        <t-empty description="未找到可加入的团队" />
      </div>
      <div v-else style="max-height: 400px; overflow-y: auto;">
        <div
            v-for="team in availableTeams"
            :key="team.teamID || team.id"
            style="padding: 12px; border: 1px solid #2a3458; border-radius: 8px; margin-bottom: 12px; cursor: pointer;"
            @click="handleJoinTeam(team)"
        >
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-weight: 600; color: #e0e6ed; margin-bottom: 4px;">{{ team.teamName || team.name }}</div>
              <div style="font-size: 12px; color: #9ca3af;">
                <span v-if="team.competitionName || team.competition?.title">
                  竞赛: {{ team.competitionName || team.competition?.title }}
                </span>
                <span v-if="team.members && team.members.length">
                  | 成员: {{ team.members.length }} 人
                </span>
              </div>
            </div>
            <t-button size="small" theme="primary">申请加入</t-button>
          </div>
        </div>
      </div>
    </t-dialog>

    <!-- 成员详情对话框 -->
    <t-dialog
        header="团队成员"
        :visible="showMembersDialog"
        @close="showMembersDialog = false"
        width="600px"
    >
      <div v-if="loadingMembers" style="text-align: center; padding: 40px;">
        <t-loading size="large" text="加载中..." />
      </div>
      <div v-else>
        <div v-if="currentTeamInfo" style="margin-bottom: 20px; padding: 16px; background: rgba(59, 130, 246, 0.1); border-radius: 8px;">
          <div style="font-size: 18px; font-weight: 600; color: #e0e6ed; margin-bottom: 8px;">
            {{ currentTeamInfo.teamName || currentTeamInfo.name }}
          </div>
          <div style="font-size: 14px; color: #9ca3af;">
            <span v-if="currentTeamInfo.competitionName || currentTeamInfo.competition?.title">
              竞赛: {{ currentTeamInfo.competitionName || currentTeamInfo.competition?.title }}
            </span>
          </div>
        </div>
        <div v-if="currentTeamMembers.length === 0" style="text-align: center; padding: 40px;">
          <t-empty description="暂无成员信息" />
        </div>
        <div v-else>
          <div class="members-list">
            <div
              v-for="(member, index) in currentTeamMembers"
              :key="member.userID || member.userId || index"
              class="member-item"
            >
              <div class="member-avatar">
                <t-icon name="user" size="24" />
              </div>
              <div class="member-info">
                <div class="member-name">
                  {{ member.userName || member.name || '未知用户' }}
                  <t-tag
                    v-if="isCaptain(member)"
                    theme="primary"
                    size="small"
                    style="margin-left: 8px;"
                  >
                    队长
                  </t-tag>
                </div>
                <div class="member-email" v-if="member.email || member.phoneNumber">
                  {{ member.email || member.phoneNumber }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <t-button theme="primary" @click="showMembersDialog = false">关闭</t-button>
      </template>
    </t-dialog>

    <!-- 编辑团队描述对话框 -->
    <t-dialog
      v-model:visible="showEditDescriptionDialog"
      header="编辑团队描述"
      width="500px"
    >
      <t-form :data="editDescriptionForm" @submit="handleUpdateDescription">
        <t-form-item label="团队描述" name="description">
          <t-textarea 
            v-model="editDescriptionForm.description" 
            placeholder="请输入团队描述" 
            :rows="5"
            :maxlength="500"
            show-word-limit
          />
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showEditDescriptionDialog = false">取消</t-button>
          <t-button theme="primary" type="submit" :loading="updatingDescription">保存</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 提交WriteUp对话框 -->
    <t-dialog
      v-model:visible="showWriteUpDialog"
      header="提交WriteUp"
      width="700px"
    >
      <t-form :data="writeUpForm" @submit="handleWriteUpSubmit" label-width="100px">
        <t-form-item label="竞赛" name="competitionId">
          <t-input 
            :value="currentWriteUpCompetition?.title || currentWriteUpCompetition?.Title || ''" 
            disabled 
          />
        </t-form-item>
        <t-form-item label="报告标题" name="title" :rules="[{ required: true, message: '请输入报告标题' }]">
          <t-input v-model="writeUpForm.title" placeholder="请输入WriteUp报告标题" :maxlength="100" show-word-limit />
        </t-form-item>
        <t-form-item label="报告内容" name="content" :rules="[{ required: true, message: '请输入报告内容' }]">
          <t-textarea 
            v-model="writeUpForm.content" 
            placeholder="请详细记录解题过程和思路..." 
            :rows="15"
            :maxlength="10000"
            show-word-limit
          />
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showWriteUpDialog = false">取消</t-button>
          <t-button theme="primary" type="submit" :loading="submittingWriteUp">提交</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 查看WriteUp对话框 -->
    <t-dialog
      v-model:visible="showViewWriteUpDialog"
      header="查看WriteUp"
      width="800px"
    >
      <div v-if="currentWriteUp" style="padding: 20px;">
        <div style="margin-bottom: 20px;">
          <h3 style="color: #e0e6ed; margin-bottom: 10px;">{{ currentWriteUp.title }}</h3>
          <div style="color: #9ca3af; font-size: 14px; margin-bottom: 20px;">
            <span>竞赛: {{ currentWriteUp.competition?.title || currentWriteUp.competitionName || '未知' }}</span>
            <span style="margin-left: 20px;">提交时间: {{ formatTime(currentWriteUp.createTime) }}</span>
          </div>
        </div>
        <div style="background: #0f1629; padding: 20px; border-radius: 8px; border: 1px solid #2a3458;">
          <div style="color: #d1d5db; white-space: pre-wrap; line-height: 1.8;">{{ currentWriteUp.content }}</div>
        </div>
      </div>
      <template #footer>
        <t-button variant="outline" @click="showViewWriteUpDialog = false">关闭</t-button>
        <t-button 
          theme="primary" 
          variant="outline" 
          @click="handleDownloadWriteUp"
          :loading="downloadingWriteUp"
        >
          <t-icon name="download" style="margin-right: 4px;" />
          下载WriteUp
        </t-button>
      </template>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTeamStore } from '@/stores/teamStore'
import { useCompetitionStore } from '@/stores/competitionStore'
import { teamService } from '@/services/teamService'
import { writeUpService } from '@/services/writeUpService'
import TeamCard from '../team/TeamCard.vue'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const teamStore = useTeamStore()
const competitionStore = useCompetitionStore()
const teams = ref([])
const loading = ref(false)
const error = ref(null)
const showCreateTeamModal = ref(false)
const showJoinTeamModal = ref(false)
const creatingTeam = ref(false)
const searchingTeams = ref(false)
const availableTeams = ref([])
const searchTeamKeyword = ref('')
const joinTeamCompetitionId = ref(null)
const loadingCompetitions = ref(false)
const competitions = ref([])
const competitionOptions = ref([])

// 成员详情对话框
const showMembersDialog = ref(false)
const currentTeamMembers = ref([])
const currentTeamInfo = ref(null)
const loadingMembers = ref(false)

// 编辑描述对话框
const showEditDescriptionDialog = ref(false)
const editingTeam = ref(null)
const updatingDescription = ref(false)
const editDescriptionForm = ref({
  description: ''
})

// WriteUp相关
const showWriteUpDialog = ref(false)
const submittingWriteUp = ref(false)
const currentWriteUpTeam = ref(null)
const currentWriteUpCompetition = ref(null)
const writeUpForm = ref({
  competitionId: null,
  title: '',
  content: ''
})

// 查看WriteUp
const showViewWriteUpDialog = ref(false)
const currentWriteUp = ref(null)
const teamWriteUps = ref([])
const downloadingWriteUp = ref(false)

const createTeamForm = ref({
  competitionId: null,
  teamName: '',
  inviteCode: '',
  description: ''
})

// 加载竞赛列表
const loadCompetitions = async () => {
  try {
    loadingCompetitions.value = true
    const comps = await competitionStore.getPublishedCompetitions()
    competitions.value = comps
    competitionOptions.value = comps.map(c => ({
      label: c.title || c.Title,
      value: c.competitionID || c.id
    }))
  } catch (err) {
    console.error('加载竞赛列表失败:', err)
  } finally {
    loadingCompetitions.value = false
  }
}

// 获取用户已加入的所有团队
const loadMyTeams = async () => {
  try {
    loading.value = true
    error.value = null
    
    // 获取所有团队（使用较大的 size 值，并支持分页）
    let allTeams = []
    let currentPage = 0
    const pageSize = 100
    let hasMore = true
    
    while (hasMore) {
      const result = await teamService.getMyTeams(currentPage, pageSize)
      console.log(`第 ${currentPage + 1} 页团队数据:`, result)
      
      const pageTeams = result && result.teams ? result.teams : (Array.isArray(result) ? result : [])
      
      if (pageTeams && pageTeams.length > 0) {
        console.log(`第 ${currentPage + 1} 页获取到 ${pageTeams.length} 个团队`)
        allTeams = allTeams.concat(pageTeams)
      }
      
      // 检查是否还有更多数据
      if (result && result.totalPages !== undefined && result.totalPages > 0) {
        hasMore = currentPage < result.totalPages - 1
        console.log(`总页数: ${result.totalPages}, 当前页: ${currentPage}, 还有更多: ${hasMore}`)
        currentPage++
      } else if (result && result.totalElements !== undefined && result.totalElements > 0) {
        hasMore = allTeams.length < result.totalElements
        console.log(`总数量: ${result.totalElements}, 已加载: ${allTeams.length}, 还有更多: ${hasMore}`)
        currentPage++
      } else {
        // 如果没有分页信息，检查当前页是否有数据
        hasMore = pageTeams && pageTeams.length === pageSize
        console.log(`当前页数据量: ${pageTeams ? pageTeams.length : 0}, 还有更多: ${hasMore}`)
        currentPage++
      }
      
      // 防止无限循环
      if (currentPage > 10) {
        console.warn('加载团队列表时达到最大页数限制')
        break
      }
      
      // 如果当前页没有数据，停止加载
      if (!pageTeams || pageTeams.length === 0) {
        console.log('当前页没有数据，停止加载')
        break
      }
    }
    
    // 确保每个团队都有必要的字段
    teams.value = allTeams.map(team => {
      // 确保字段映射正确
      return {
        ...team,
        teamID: team.teamID || team.id,
        teamName: team.teamName || team.name,
        competitionID: team.competitionID || team.competition?.competitionID || team.competitionId,
        competitionName: team.competitionName || team.competition?.title || team.competition?.Title,
        captainName: team.captainName || team.captain?.userName || team.captain?.userName,
        members: team.members || [],
        description: team.description || '',
        auditState: team.auditState || '0',
        creationTime: team.creationTime || team.creationTime
      }
    })
    console.log('最终加载的团队列表:', teams.value.length, '个团队', teams.value)
  } catch (err) {
    console.error('加载团队列表失败:', err)
    error.value = err.message || '加载团队列表失败'
    MessagePlugin.error('加载团队列表失败: ' + (err.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 创建团队
const handleCreateTeam = async () => {
  if (!createTeamForm.value.teamName) {
    MessagePlugin.warning('请输入团队名称')
    return
  }
  if (!createTeamForm.value.competitionId) {
    MessagePlugin.warning('请选择竞赛')
    return
  }

  // 检查用户是否已在该竞赛中有团队
  try {
    const hasTeam = await teamService.hasTeamForCompetition(createTeamForm.value.competitionId)
    if (hasTeam) {
      MessagePlugin.warning('您已在该竞赛中加入了团队，每个竞赛只能加入一个团队')
      return
    }
  } catch (error) {
    console.error('检查团队状态失败:', error)
    // 继续执行，让后端验证
  }

  try {
    creatingTeam.value = true
    await teamStore.createTeam({
      teamName: createTeamForm.value.teamName,
      competitionID: createTeamForm.value.competitionId,
      competitionId: createTeamForm.value.competitionId,
      inviteCode: createTeamForm.value.inviteCode,
      description: createTeamForm.value.description || null
    })

    MessagePlugin.success('团队创建成功')
    showCreateTeamModal.value = false
    createTeamForm.value = {
      competitionId: null,
      teamName: '',
      inviteCode: '',
      description: ''
    }
    // 重新加载团队列表
    await loadMyTeams()
  } catch (error) {
    // 后端返回的错误消息应该已经包含了"您已加入该竞赛的其他战队"等信息
    MessagePlugin.error(error.message || '创建团队失败')
  } finally {
    creatingTeam.value = false
  }
}

// 搜索团队
const searchTeams = async () => {
  try {
    searchingTeams.value = true
    const params = {
      page: 0,
      size: 50
    }
    if (searchTeamKeyword.value.trim()) {
      params.keyword = searchTeamKeyword.value.trim()
    }
    if (joinTeamCompetitionId.value) {
      params.competitionId = joinTeamCompetitionId.value
    }
    
    const result = await teamService.getTeams(params.page, params.size, params.competitionId, null, params.keyword)
    availableTeams.value = result.teams || []
  } catch (err) {
    console.error('搜索团队失败:', err)
    MessagePlugin.error('搜索团队失败: ' + (err.message || '未知错误'))
    availableTeams.value = []
  } finally {
    searchingTeams.value = false
  }
}

// 处理创建团队按钮点击
const handleCreateTeamClick = () => {
  console.log('创建团队按钮被点击')
  showCreateTeamModal.value = true
}

// 处理加入团队按钮点击
const handleJoinTeamClick = () => {
  console.log('加入团队按钮被点击')
  showJoinTeamModal.value = true
}

// 加入团队
const handleJoinTeam = async (team) => {
  try {
    const teamId = team.teamID || team.id
    if (!teamId) {
      MessagePlugin.error('无效的团队ID')
      return
    }

    // 检查用户是否已在该竞赛中有团队
    const competitionId = team.competitionID || team.competition?.competitionID || team.competitionId
    if (competitionId) {
      try {
        const hasTeam = await teamService.hasTeamForCompetition(competitionId)
        if (hasTeam) {
          MessagePlugin.warning('您已在该竞赛中加入了团队，每个竞赛只能加入一个团队')
          return
        }
      } catch (error) {
        console.error('检查团队状态失败:', error)
        // 继续执行，让后端验证
      }
    }
    
    await teamService.joinTeam(teamId, '')
    MessagePlugin.success('申请已提交，等待队长审核')
    showJoinTeamModal.value = false
    // 重新加载团队列表
    await loadMyTeams()
  } catch (error) {
    // 后端返回的错误消息应该已经包含了"您已加入该竞赛的其他战队"等信息
    const errorMessage = error.message || '申请加入团队失败'
    MessagePlugin.error(errorMessage)
  }
}

// 判断是否为队长
const isCaptain = (member) => {
  if (!member) return false
  
  // 优先使用后端返回的 isCaptain 标记
  if (member.isCaptain !== undefined) {
    return member.isCaptain === true
  }
  
  // 如果没有 isCaptain 标记，则通过比较 ID 判断
  if (!currentTeamInfo.value) return false
  
  const captainId = currentTeamInfo.value.captain?.userID || 
                    currentTeamInfo.value.captainID || 
                    currentTeamInfo.value.captain?.userId ||
                    (currentTeamInfo.value.captain && typeof currentTeamInfo.value.captain === 'number' ? currentTeamInfo.value.captain : null)
  
  const memberId = member.userID || member.userId
  
  if (!captainId || !memberId) return false
  
  // 确保类型一致后比较
  return String(captainId) === String(memberId)
}

// 查看成员详情
const handleViewMembers = async (team) => {
  try {
    loadingMembers.value = true
    showMembersDialog.value = true
    currentTeamInfo.value = team
    
    // 获取团队详情（包含成员列表）
    const teamDetail = await teamService.getTeamById(team.teamID || team.id)
    currentTeamInfo.value = teamDetail // 更新为完整的团队详情
    currentTeamMembers.value = teamDetail.members || team.members || []
    
    console.log('团队详情:', currentTeamInfo.value)
    console.log('团队成员:', currentTeamMembers.value)
    console.log('队长ID:', currentTeamInfo.value.captain?.userID || currentTeamInfo.value.captainID)
  } catch (error) {
    console.error('获取团队成员失败:', error)
    MessagePlugin.error('获取团队成员失败: ' + (error.message || '未知错误'))
    currentTeamMembers.value = []
  } finally {
    loadingMembers.value = false
  }
}

// 编辑团队描述
const handleEditDescription = (team) => {
  editingTeam.value = team
  editDescriptionForm.value.description = team.description || ''
  showEditDescriptionDialog.value = true
}

// 更新团队描述
const handleUpdateDescription = async () => {
  if (!editingTeam.value) return
  
  try {
    updatingDescription.value = true
    const teamId = editingTeam.value.teamID || editingTeam.value.id
    await teamService.updateTeam(teamId, {
      description: editDescriptionForm.value.description || null
    })
    
    MessagePlugin.success({
      content: '团队描述更新成功',
      duration: 3000,
      icon: true
    })
    
    showEditDescriptionDialog.value = false
    editingTeam.value = null
    editDescriptionForm.value.description = ''
    
    // 重新加载团队列表
    await loadMyTeams()
  } catch (error) {
    MessagePlugin.error({
      content: error.message || '更新团队描述失败',
      duration: 3000
    })
  } finally {
    updatingDescription.value = false
  }
}

// 解散团队
const handleDisbandTeam = async (team) => {
  const { DialogPlugin } = await import('tdesign-vue-next')
  
  DialogPlugin.confirm({
    header: '确认解散团队',
    body: `确定要解散团队 "${team.teamName || team.name}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      try {
        const teamId = team.teamID || team.id
        await teamService.disbandTeam(teamId)
        
        MessagePlugin.success({
          content: '团队解散成功',
          duration: 3000,
          icon: true
        })
        
        // 重新加载团队列表
        await loadMyTeams()
      } catch (error) {
        MessagePlugin.error({
          content: error.message || '解散团队失败',
          duration: 3000
        })
      }
    }
  })
}

// 提交WriteUp
const handleSubmitWriteUp = async (team) => {
  try {
    currentWriteUpTeam.value = team
    const competitionId = team.competitionID || team.competition?.competitionID || team.competitionId
    
    if (!competitionId) {
      MessagePlugin.error('无法获取竞赛信息')
      return
    }
    
    // 查找竞赛信息
    const competition = competitions.value.find(c => 
      (c.competitionID || c.id) === competitionId
    )
    currentWriteUpCompetition.value = competition || { title: '未知竞赛' }
    
    // 检查是否已有WriteUp
    const existingWriteUps = await writeUpService.getWriteUpsByCompetition(competitionId)
    const myWriteUp = existingWriteUps.find(w => 
      w.competition?.competitionID === competitionId || w.competitionID === competitionId
    )
    
    if (myWriteUp) {
      // 如果已有WriteUp，填充表单
      writeUpForm.value = {
        competitionId: competitionId,
        title: myWriteUp.title || '',
        content: myWriteUp.content || ''
      }
      MessagePlugin.info('您已提交过WriteUp，可以更新内容')
    } else {
      // 新建WriteUp
      writeUpForm.value = {
        competitionId: competitionId,
        title: '',
        content: ''
      }
    }
    
    showWriteUpDialog.value = true
  } catch (error) {
    MessagePlugin.error('加载WriteUp信息失败: ' + (error.message || '未知错误'))
  }
}

// 提交WriteUp表单
const handleWriteUpSubmit = async () => {
  if (!writeUpForm.value.title || !writeUpForm.value.title.trim()) {
    MessagePlugin.warning('请输入报告标题')
    return
  }
  if (!writeUpForm.value.content || !writeUpForm.value.content.trim()) {
    MessagePlugin.warning('请输入报告内容')
    return
  }
  
  try {
    submittingWriteUp.value = true
    await writeUpService.uploadWriteUp(
      writeUpForm.value.competitionId,
      writeUpForm.value.title,
      writeUpForm.value.content
    )
    
    MessagePlugin.success('WriteUp提交成功')
    showWriteUpDialog.value = false
    writeUpForm.value = {
      competitionId: null,
      title: '',
      content: ''
    }
    currentWriteUpTeam.value = null
    currentWriteUpCompetition.value = null
  } catch (error) {
    MessagePlugin.error('提交WriteUp失败: ' + (error.message || '未知错误'))
  } finally {
    submittingWriteUp.value = false
  }
}

// 查看WriteUp
const handleViewWriteUp = async (team) => {
  try {
    const competitionId = team.competitionID || team.competition?.competitionID || team.competitionId
    if (!competitionId) {
      MessagePlugin.error('无法获取竞赛信息')
      return
    }
    
    const writeUps = await writeUpService.getWriteUpsByCompetition(competitionId)
    const myWriteUp = writeUps.find(w => 
      w.competition?.competitionID === competitionId || w.competitionID === competitionId
    )
    
    if (!myWriteUp) {
      MessagePlugin.warning('您尚未提交WriteUp')
      return
    }
    
    currentWriteUp.value = myWriteUp
    showViewWriteUpDialog.value = true
  } catch (error) {
    MessagePlugin.error('加载WriteUp失败: ' + (error.message || '未知错误'))
  }
}

// 下载WriteUp
const handleDownloadWriteUp = async () => {
  if (!currentWriteUp.value || !currentWriteUp.value.writeUpID) {
    MessagePlugin.warning('没有可下载的WriteUp')
    return
  }
  
  try {
    downloadingWriteUp.value = true
    await writeUpService.downloadWriteUp(currentWriteUp.value.writeUpID)
    MessagePlugin.success('WriteUp下载成功')
  } catch (error) {
    MessagePlugin.error('下载WriteUp失败: ' + (error.message || '未知错误'))
  } finally {
    downloadingWriteUp.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await loadCompetitions()
  await loadMyTeams()
})
</script>

<style scoped>
.teams-page {
  animation: fadeIn 0.3s ease-in-out;
  position: relative;
  z-index: 1;
}

.card {
  background: #1a1f3a;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  border: 1px solid #2a3458;
  position: relative;
  z-index: 1;
}

/* 确保按钮可以点击 */
.teams-page :deep(.t-button) {
  position: relative;
  z-index: 10;
  pointer-events: auto !important;
  cursor: pointer !important;
}

.teams-page :deep(.t-dialog) {
  z-index: 1000;
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

.members-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.member-item:hover {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 16px;
  font-weight: 500;
  color: #e0e6ed;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.member-email {
  font-size: 12px;
  color: #9ca3af;
}
</style>
