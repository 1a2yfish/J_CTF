<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">管理后台</h1>
        <p class="page-subtitle">管理系统用户、竞赛、团队和题目</p>
      </div>
    </div>

    <t-tabs v-model="activeTab" theme="normal" @change="handleTabChange">
      <t-tab-panel value="dashboard" label="仪表盘">
        <div class="tab-content">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
                <t-icon name="user" size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                <t-icon name="flag" size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalCompetitions || 0 }}</div>
                <div class="stat-label">总竞赛数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
                <t-icon name="team" size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalTeams || 0 }}</div>
                <div class="stat-label">总团队数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(239, 68, 68, 0.1); color: #ef4444;">
                <t-icon name="file-code" size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.totalChallenges || 0 }}</div>
                <div class="stat-label">总题目数</div>
              </div>
            </div>
          </div>
        </div>
      </t-tab-panel>

      <!-- 用户管理 -->
      <t-tab-panel value="users" label="用户管理">
        <div class="tab-content">
          <div class="table-header">
            <t-input
              v-model="userSearchKeyword"
              placeholder="搜索用户..."
              clearable
              style="width: 300px;"
              @enter="loadUsers"
            >
              <template #prefix-icon>
                <t-icon name="search" />
              </template>
            </t-input>
            <t-button theme="primary" @click="loadUsers">刷新</t-button>
          </div>
          <t-table
            :data="users"
            :columns="userColumns"
            :loading="loadingUsers"
            row-key="userID"
            :pagination="userPagination"
            @page-change="handleUserPageChange"
          >
            <template #userType="{ row }">
              <t-tag :theme="row.userType === 'ADMIN' ? 'danger' : 'primary'">
                {{ row.userType === 'ADMIN' ? '管理员' : '普通用户' }}
              </t-tag>
            </template>
            <template #adminRole="{ row }">
              <span v-if="row.userType === 'ADMIN'">{{ row.adminRole || 'SYSTEM' }}</span>
              <span v-else style="color: #9ca3af;">-</span>
            </template>
            <template #userEmail="{ row }">
              <span>{{ row.userEmail || '-' }}</span>
            </template>
            <template #actions="{ row }">
              <t-button variant="text" size="small" @click="editUser(row)">编辑</t-button>
              <t-button 
                variant="text" 
                size="small" 
                theme="danger"
                @click="deleteUser(row)"
              >
                删除
              </t-button>
            </template>
          </t-table>
        </div>
      </t-tab-panel>

      <!-- 竞赛管理 -->
      <t-tab-panel value="competitions" label="竞赛管理">
        <div class="tab-content">
          <!-- 竞赛列表部分 -->
          <div class="competition-section">
            <div class="section-header">
              <h3 class="section-title">竞赛列表</h3>
              <div class="section-actions">
                <t-button theme="primary" @click="showCreateCompetition = true">
                  <t-icon name="add" /> 创建竞赛
                </t-button>
                <t-button @click="loadCompetitions">刷新</t-button>
              </div>
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
                <t-button 
                  variant="text" 
                  size="small" 
                  theme="primary"
                  @click="manageCompetitionChallenges(row)"
                >
                  管理题目
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

          <!-- 题目管理部分 -->
          <div class="challenge-section" style="margin-top: 32px;">
            <div class="section-header">
              <h3 class="section-title">题目管理</h3>
              <div class="section-actions">
                <t-button 
                  theme="primary" 
                  @click="handleCreateChallengeClick"
                  :disabled="!challengeCompetitionFilter && !managingCompetitionId"
                >
                  <t-icon name="add" /> 创建题目
                </t-button>
                <t-select
                  v-model="challengeCompetitionFilter"
                  placeholder="筛选竞赛"
                  style="width: 200px;"
                  clearable
                  @change="handleCompetitionFilterChange"
                >
                  <t-option
                    v-for="comp in competitions"
                    :key="comp.competitionID || comp.id"
                    :value="comp.competitionID || comp.id"
                    :label="comp.title || comp.Title"
                  />
                </t-select>
                <t-button @click="loadChallenges">刷新</t-button>
              </div>
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
        </div>
      </t-tab-panel>

      <!-- 团队审核 -->
      <t-tab-panel value="teams" label="团队审核">
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
      </t-tab-panel>

      <!-- Flag提交 -->
      <t-tab-panel value="flag-submissions" label="Flag提交">
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
      </t-tab-panel>
    </t-tabs>

    <!-- 创建/编辑竞赛对话框 -->
    <t-dialog
      v-model:visible="showCreateCompetition"
      :header="editingCompetition ? '编辑竞赛' : '创建竞赛'"
      width="600px"
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
            <t-option value="ONGOING" label="进行中" />
            <t-option value="FINISHED" label="已结束" />
            <t-option value="CANCELLED" label="已取消" />
          </t-select>
        </t-form-item>
        <t-form-item v-if="editingCompetition" label="审核状态" name="auditStatus">
          <t-select v-model="competitionForm.auditStatus" placeholder="请选择审核状态">
            <t-option value="PENDING" label="待审核" />
            <t-option value="APPROVED" label="已通过" />
            <t-option value="REJECTED" label="已拒绝" />
          </t-select>
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showCreateCompetition = false">取消</t-button>
          <t-button theme="primary" type="submit">保存</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 拒绝团队对话框 -->
    <t-dialog
      v-model:visible="showRejectTeamDialog"
      header="拒绝团队"
      width="400px"
    >
      <t-form :data="rejectTeamForm" @submit="handleRejectTeam">
        <t-form-item label="拒绝原因" name="auditRemark">
          <t-textarea v-model="rejectTeamForm.auditRemark" placeholder="请输入拒绝原因" :rows="3" />
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showRejectTeamDialog = false">取消</t-button>
          <t-button theme="primary" type="submit">确认拒绝</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 拒绝竞赛对话框 -->
    <t-dialog
      v-model:visible="showRejectCompetitionDialog"
      header="拒绝竞赛"
      width="500px"
    >
      <t-form :data="rejectCompetitionForm" @submit="handleRejectCompetition">
        <t-form-item label="拒绝原因" name="auditRemark">
          <t-textarea v-model="rejectCompetitionForm.auditRemark" placeholder="请输入拒绝原因" :rows="3" />
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showRejectCompetitionDialog = false">取消</t-button>
          <t-button theme="primary" type="submit">确认拒绝</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 创建/编辑题目对话框 -->
    <t-dialog
      v-model:visible="showCreateChallenge"
      :header="editingChallenge ? '编辑题目' : '创建题目'"
      width="700px"
    >
      <t-form :data="challengeForm" @submit="handleChallengeSubmit">
        <t-form-item label="竞赛" name="competitionID" required>
          <t-select
            v-model="challengeForm.competitionID"
            placeholder="请选择竞赛"
            :options="competitionOptions"
            :disabled="!!editingChallenge"
          />
        </t-form-item>
        <t-form-item label="题目标题" name="title" required>
          <t-input v-model="challengeForm.title" placeholder="请输入题目标题" />
        </t-form-item>
        <t-form-item label="题目描述" name="description" required>
          <t-textarea v-model="challengeForm.description" placeholder="请输入题目描述" :rows="5" />
        </t-form-item>
        <t-form-item label="分类" name="category" required>
          <t-select v-model="challengeForm.category" placeholder="请选择分类">
            <t-option value="Web" label="Web" />
            <t-option value="Pwn" label="Pwn" />
            <t-option value="Crypto" label="Crypto" />
            <t-option value="Reverse" label="Reverse" />
            <t-option value="Misc" label="Misc" />
          </t-select>
        </t-form-item>
        <t-form-item label="难度" name="difficulty" required>
          <t-select v-model="challengeForm.difficulty" placeholder="请选择难度">
            <t-option value="Easy" label="简单" />
            <t-option value="Medium" label="中等" />
            <t-option value="Hard" label="困难" />
          </t-select>
        </t-form-item>
        <t-form-item label="分值" name="points" required>
          <t-input-number v-model="challengeForm.points" :min="1" />
        </t-form-item>
        <t-form-item label="Flag" name="flag" required>
          <t-input v-model="challengeForm.flag" placeholder="请输入Flag" type="password" />
        </t-form-item>
        <t-form-item label="提示" name="hint">
          <t-textarea v-model="challengeForm.hint" placeholder="请输入提示（可选）" :rows="3" />
        </t-form-item>
        <t-form-item label="附件链接" name="attachmentUrl">
          <t-input v-model="challengeForm.attachmentUrl" placeholder="请输入附件下载链接（可选）" />
        </t-form-item>
        <template #footer>
          <t-button variant="outline" @click="showCreateChallenge = false">取消</t-button>
          <t-button theme="primary" type="submit">保存</t-button>
        </template>
      </t-form>
    </t-dialog>

    <!-- 编辑用户对话框 -->
    <t-dialog
      v-model:visible="showEditUser"
      header="编辑用户"
      width="600px"
      :confirm-btn="{ theme: 'primary' }"
      @confirm="handleUserSubmit"
    >
      <t-form :data="userForm" label-width="100px">
        <t-form-item label="用户名" name="userName">
          <t-input v-model="userForm.userName" placeholder="请输入用户名" />
        </t-form-item>
        <t-form-item label="邮箱" name="userEmail">
          <t-input v-model="userForm.userEmail" placeholder="请输入邮箱" />
        </t-form-item>
        <t-form-item label="手机号" name="phoneNumber">
          <t-input v-model="userForm.phoneNumber" placeholder="请输入手机号" />
        </t-form-item>
        <t-form-item label="用户类型" name="userType">
          <t-select v-model="userForm.userType" placeholder="请选择用户类型">
            <t-option value="ADMIN" label="管理员" />
            <t-option value="ORDINARY" label="普通用户" />
          </t-select>
        </t-form-item>
        <t-form-item v-if="userForm.userType === 'ADMIN'" label="管理员角色" name="adminRole">
          <t-input v-model="userForm.adminRole" placeholder="请输入管理员角色" />
        </t-form-item>
        <t-form-item v-if="userForm.userType === 'ORDINARY'" label="用户状态" name="userStatus">
          <t-switch v-model="userForm.userStatus" />
          <span style="margin-left: 8px;">{{ userForm.userStatus ? '正常' : '禁用' }}</span>
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminService } from '@/services/adminService'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

const activeTab = ref('dashboard')
const tabs = [
  { value: 'dashboard', label: '仪表盘' },
  { value: 'users', label: '用户管理' },
  { value: 'competitions', label: '竞赛管理' },
  { value: 'teams', label: '团队审核' },
  { value: 'flag-submissions', label: 'Flag提交' }
]

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalCompetitions: 0,
  totalTeams: 0,
  totalChallenges: 0
})

// 用户管理
const users = ref([])
const loadingUsers = ref(false)
const userSearchKeyword = ref('')
const userPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})
const userColumns = [
  { colKey: 'userID', title: 'ID', width: 80 },
  { colKey: 'userName', title: '用户名', width: 150 },
  { colKey: 'userEmail', title: '邮箱', width: 200 },
  { colKey: 'userType', title: '用户类型', width: 120 },
  { colKey: 'adminRole', title: '角色', width: 100 },
  { colKey: 'createTime', title: '创建时间', width: 180 },
  { colKey: 'actions', title: '操作', width: 150, fixed: 'right' }
]

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
  { colKey: 'actions', title: '操作', width: 200, fixed: 'right' }
]
const showRejectTeamDialog = ref(false)
const rejectingTeam = ref(null)
const rejectTeamForm = ref({
  auditRemark: ''
})

// 审核竞赛相关
const showRejectCompetitionDialog = ref(false)
const rejectingCompetition = ref(null)
const rejectCompetitionForm = ref({
  auditRemark: ''
})

// 题目管理
const challenges = ref([])
const loadingChallenges = ref(false)
const challengeCompetitionFilter = ref(null)
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
const managingCompetitionId = ref(null) // 当前正在管理题目的竞赛ID
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
const competitionOptions = ref([])

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

// 加载仪表盘数据
const loadDashboard = async () => {
  try {
    const data = await adminService.getDashboard()
    stats.value = {
      totalUsers: data.totalUsers || 0,
      totalCompetitions: data.totalCompetitions || 0,
      totalTeams: data.totalTeams || 0,
      totalChallenges: data.totalChallenges || 0
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    MessagePlugin.error('加载仪表盘数据失败')
  }
}

// 加载用户列表
const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const data = await adminService.getUsers(
      userPagination.value.current - 1,
      userPagination.value.pageSize,
      'createTime',
      userSearchKeyword.value || null
    )
    users.value = data.users || []
    userPagination.value.total = data.totalElements || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    MessagePlugin.error('加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

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
    console.error('加载竞赛列表失败:', error)
    MessagePlugin.error('加载竞赛列表失败')
  } finally {
    loadingCompetitions.value = false
  }
}

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
    console.error('加载团队列表失败:', error)
    MessagePlugin.error('加载团队列表失败')
  } finally {
    loadingTeams.value = false
  }
}

// 标签页切换
const handleTabChange = (value) => {
  activeTab.value = value
  if (value === 'dashboard') {
    loadDashboard()
  } else if (value === 'users') {
    loadUsers()
  } else if (value === 'competitions') {
    loadCompetitions()
    loadCompetitionsForFilter()
    // 加载题目列表（如果有筛选条件）
    if (challengeCompetitionFilter.value || managingCompetitionId.value) {
      loadChallenges()
    }
  } else if (value === 'teams') {
    loadTeams()
  } else if (value === 'flag-submissions') {
    loadFlagSubmissions()
    loadCompetitionsForFilter()
  }
}

// 竞赛筛选变化处理
const handleCompetitionFilterChange = () => {
  if (challengeCompetitionFilter.value) {
    managingCompetitionId.value = challengeCompetitionFilter.value
  } else {
    managingCompetitionId.value = null
  }
  loadChallenges()
}

// 用户分页
const handleUserPageChange = (pageInfo) => {
  userPagination.value.current = pageInfo.current
  loadUsers()
}

// 竞赛分页
const handleCompetitionPageChange = (pageInfo) => {
  competitionPagination.value.current = pageInfo.current
  loadCompetitions()
}

// 团队分页
const handleTeamPageChange = (pageInfo) => {
  teamPagination.value.current = pageInfo.current
  loadTeams()
}

// 编辑用户
const showEditUser = ref(false)
const editingUser = ref(null)
const userForm = ref({
  userName: '',
  userEmail: '',
  phoneNumber: '',
  userType: '',
  adminRole: '',
  userStatus: true
})

const editUser = async (user) => {
  try {
    // 获取用户详情
    const userDetails = await adminService.getUserDetails(user.userID)
    editingUser.value = userDetails
    userForm.value = {
      userName: userDetails.userName || '',
      userEmail: userDetails.userEmail || '',
      phoneNumber: userDetails.phoneNumber || '',
      userType: userDetails.userType || 'ORDINARY',
      adminRole: userDetails.adminRole || 'SYSTEM',
      userStatus: userDetails.userStatus !== false
    }
    showEditUser.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    MessagePlugin.error('获取用户详情失败: ' + (error.message || '未知错误'))
  }
}

// 提交用户表单
const handleUserSubmit = async () => {
  if (!editingUser.value) return
  
  try {
    await adminService.updateUser(editingUser.value.userID, userForm.value)
    MessagePlugin.success('更新成功')
    showEditUser.value = false
    editingUser.value = null
    loadUsers()
  } catch (error) {
    console.error('更新用户失败:', error)
    MessagePlugin.error('更新失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 删除用户
const deleteUser = async (user) => {
  // 检查是否是当前登录用户
  const { useAuthStore } = await import('@/stores/authStore')
  const authStore = useAuthStore()
  const currentUser = authStore.user
  if (currentUser && (currentUser.userID === user.userID || currentUser.userId === user.userID)) {
    MessagePlugin.error('不能删除当前登录的用户')
    return
  }
  
  // 显示确认对话框，等待用户确认
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除用户 "${user.userName}" 吗？此操作将删除该用户的所有数据（包括团队、得分等），且不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      // 用户点击确认按钮后执行删除
      try {
        await adminService.deleteUser(user.userID)
        MessagePlugin.success('删除成功')
        // 刷新用户列表和仪表盘数据
        loadUsers()
        if (activeTab.value === 'dashboard') {
          loadDashboard()
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        MessagePlugin.error('删除失败: ' + (error.response?.data?.message || error.message || '未知错误'))
      }
    },
    onCancel: () => {
      // 用户点击取消，不执行任何操作
    }
  })
}

// 编辑竞赛
const editCompetition = async (competition) => {
  try {
    // 获取竞赛详情以确保获取完整数据
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

// 管理竞赛题目
const manageCompetitionChallenges = (competition) => {
  const competitionId = competition.competitionID || competition.id
  managingCompetitionId.value = competitionId
  
  // 设置竞赛筛选
  challengeCompetitionFilter.value = competitionId
  
  // 加载该竞赛的题目列表
  loadChallenges()
  
  // 确保竞赛列表已加载（用于创建题目时的选择）
  if (competitions.value.length === 0) {
    loadCompetitionsForFilter()
  }
  
  // 滚动到题目管理部分
  setTimeout(() => {
    const challengeSection = document.querySelector('.challenge-section')
    if (challengeSection) {
      challengeSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100)
}

// 删除竞赛
const deleteCompetition = async (competition) => {
  // 显示确认对话框，等待用户确认
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除竞赛 "${competition.title || competition.Title}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      // 用户点击确认按钮后执行删除
      try {
        await adminService.deleteCompetition(competition.competitionID || competition.id)
        MessagePlugin.success('删除成功')
        loadCompetitions()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    },
    onCancel: () => {
      // 用户点击取消，不执行任何操作
    }
  })
}

// 提交竞赛表单
const handleCompetitionSubmit = async ({ validateResult, firstError }) => {
  // 表单验证失败
  if (validateResult !== true) {
    MessagePlugin.warning(firstError)
    return
  }
  
  try {
    let competitionId = null
    if (editingCompetition.value) {
      competitionId = editingCompetition.value.competitionID || editingCompetition.value.id
      await adminService.updateCompetition(competitionId, competitionForm.value)
      MessagePlugin.success('更新成功')
    } else {
      const result = await adminService.createCompetition(competitionForm.value)
      competitionId = result.competitionID || result.id
      MessagePlugin.success('创建成功')
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
    
    // 如果是新建竞赛，自动打开创建题目对话框
    if (competitionId && !editingCompetition.value) {
      // 等待竞赛列表刷新完成
      setTimeout(() => {
        // 设置竞赛筛选和管理状态
        challengeCompetitionFilter.value = competitionId
        managingCompetitionId.value = competitionId
        // 重置题目表单并设置竞赛ID
        challengeForm.value = {
          competitionID: competitionId,
          title: '',
          description: '',
          category: '',
          difficulty: '',
          points: 100,
          flag: '',
          hint: '',
          attachmentUrl: ''
        }
        editingChallenge.value = null
        // 加载该竞赛的题目列表
        loadChallenges()
        // 打开创建题目对话框
        showCreateChallenge.value = true
        // 滚动到题目管理部分
        setTimeout(() => {
          const challengeSection = document.querySelector('.challenge-section')
          if (challengeSection) {
            challengeSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }
        }, 300)
      }, 500)
    }
  } catch (error) {
    MessagePlugin.error('操作失败: ' + (error.message || '未知错误'))
  }
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

// 审核竞赛
const auditCompetition = async (competition, approved, auditRemark = '') => {
  try {
    await adminService.auditCompetition(
      competition.competitionID || competition.id,
      approved,
      auditRemark
    )
    MessagePlugin.success('审核成功')
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

// 删除团队
const deleteTeam = async (team) => {
  // 显示确认对话框，等待用户确认
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除团队 "${team.teamName || team.name}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      // 用户点击确认按钮后执行删除
      try {
        await adminService.deleteTeam(team.teamID || team.id)
        MessagePlugin.success('删除成功')
        loadTeams()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    },
    onCancel: () => {
      // 用户点击取消，不执行任何操作
    }
  })
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

// 加载竞赛列表用于筛选
const loadCompetitionsForFilter = async () => {
  try {
    const data = await adminService.getCompetitions(0, 100)
    competitions.value = data.competitions || []
    competitionOptions.value = competitions.value.map(c => ({
      label: c.title || c.Title,
      value: c.competitionID || c.id
    }))
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
  }
}

// 加载题目列表
const loadChallenges = async () => {
  loadingChallenges.value = true
  try {
    const params = {
      page: challengePagination.value.current - 1,
      size: challengePagination.value.pageSize
    }
    if (challengeCompetitionFilter.value) {
      params.competitionId = challengeCompetitionFilter.value
    }
    const response = await adminService.getChallenges(params)
    challenges.value = response.challenges || []
    challengePagination.value.total = response.totalElements || 0
  } catch (error) {
    console.error('加载题目列表失败:', error)
    MessagePlugin.error('加载题目列表失败')
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
  // 如果正在管理某个竞赛，自动设置竞赛ID
  if (managingCompetitionId.value) {
    challengeForm.value.competitionID = managingCompetitionId.value
    challengeCompetitionFilter.value = managingCompetitionId.value
  } else if (challengeCompetitionFilter.value) {
    challengeForm.value.competitionID = challengeCompetitionFilter.value
  }
  
  // 如果没有选择竞赛，提示用户
  if (!challengeForm.value.competitionID) {
    MessagePlugin.warning('请先选择竞赛')
    return
  }
  
  editingChallenge.value = null
  showCreateChallenge.value = true
}

// 编辑题目
const editChallenge = async (challenge) => {
  try {
    const details = await adminService.getChallengeDetails(challenge.challengeID || challenge.id)
    editingChallenge.value = details
    const competitionId = details.competition?.competitionID || details.competitionID
    challengeForm.value = {
      competitionID: competitionId,
      title: details.title || '',
      description: details.description || '',
      category: details.category || '',
      difficulty: details.difficulty || '',
      points: details.points || 100,
      flag: details.flag || '',
      hint: details.hint || '',
      attachmentUrl: details.attachmentUrl || ''
    }
    // 如果正在管理某个竞赛的题目，确保筛选器设置正确
    if (competitionId && managingCompetitionId.value === competitionId) {
      challengeCompetitionFilter.value = competitionId
    }
    showCreateChallenge.value = true
  } catch (error) {
    MessagePlugin.error('获取题目详情失败: ' + (error.message || '未知错误'))
  }
}

// 删除题目
const deleteChallenge = async (challenge) => {
  // 显示确认对话框，等待用户确认
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除题目 "${challenge.title}" 吗？此操作不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      // 用户点击确认按钮后执行删除
      try {
        await adminService.deleteChallenge(challenge.challengeID || challenge.id)
        MessagePlugin.success('删除成功')
        loadChallenges()
      } catch (error) {
        MessagePlugin.error('删除失败: ' + (error.message || '未知错误'))
      }
    },
    onCancel: () => {
      // 用户点击取消，不执行任何操作
    }
  })
}

// 提交题目表单
const handleChallengeSubmit = async () => {
  try {
    if (editingChallenge.value) {
      await adminService.updateChallenge(
        editingChallenge.value.challengeID || editingChallenge.value.id,
        challengeForm.value
      )
      MessagePlugin.success('更新成功')
    } else {
      await adminService.createChallenge(challengeForm.value)
      MessagePlugin.success('创建成功')
    }
    showCreateChallenge.value = false
    editingChallenge.value = null
    const savedCompetitionId = challengeForm.value.competitionID
    challengeForm.value = {
      competitionID: managingCompetitionId.value || savedCompetitionId || null,
      title: '',
      description: '',
      category: '',
      difficulty: '',
      points: 100,
      flag: '',
      hint: '',
      attachmentUrl: ''
    }
    // 如果正在管理某个竞赛的题目，保持筛选器
    if (managingCompetitionId.value) {
      challengeCompetitionFilter.value = managingCompetitionId.value
    }
    loadChallenges()
  } catch (error) {
    MessagePlugin.error('操作失败: ' + (error.message || '未知错误'))
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
    // 映射后端返回的嵌套数据结构到前端需要的扁平结构
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
    console.error('加载Flag提交列表失败:', error)
    MessagePlugin.error('加载Flag提交列表失败')
  } finally {
    loadingSubmissions.value = false
  }
}

// Flag提交分页
const handleSubmissionPageChange = (pageInfo) => {
  submissionPagination.value.current = pageInfo.current
  loadFlagSubmissions()
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
  loadDashboard()
})
</script>

<style scoped>
.admin-page {
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

.sub-tab-content {
  padding: 16px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #e0e6ed;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #9ca3af;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.competition-section,
.challenge-section {
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #2a3458;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #e0e6ed;
  margin: 0;
}

.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
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

