<template>
  <div class="user-management-page">
    <div class="page-header">
      <div class="header-content">
        <div style="display: flex; align-items: center; gap: 16px;">
          <t-button variant="text" @click="goBack">
            <t-icon name="chevron-left" /> 返回
          </t-button>
          <div>
            <h1 class="page-title">用户管理</h1>
            <p class="page-subtitle">管理系统用户信息</p>
          </div>
        </div>
      </div>
    </div>

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
import { useRouter } from 'vue-router'
import { adminService } from '@/services/adminService'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { showSuccess, handleApiError } from '@/utils/message'
import { error as logError } from '@/utils/logger'

const router = useRouter()

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

// 返回上一页
const goBack = () => {
  router.back()
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
    logError('加载用户列表失败:', error)
    handleApiError(error, '加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

// 用户分页
const handleUserPageChange = (pageInfo) => {
  userPagination.value.current = pageInfo.current
  loadUsers()
}

const editUser = async (user) => {
  try {
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
  const { useAuthStore } = await import('@/stores/authStore')
  const authStore = useAuthStore()
  const currentUser = authStore.user
  if (currentUser && (currentUser.userID === user.userID || currentUser.userId === user.userID)) {
    MessagePlugin.error('不能删除当前登录的用户')
    return
  }
  
  DialogPlugin.confirm({
    header: '确认删除',
    body: `确定要删除用户 "${user.userName}" 吗？此操作将删除该用户的所有数据（包括团队、得分等），且不可恢复。`,
    confirmBtn: { theme: 'danger' },
    onConfirm: async () => {
      try {
        await adminService.deleteUser(user.userID)
        showSuccess('删除成功')
        loadUsers()
      } catch (error) {
        logError('删除用户失败:', error)
        handleApiError(error, '删除失败')
      }
    }
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management-page {
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

