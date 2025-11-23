<template>
  <div class="notifications-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">通知中心</h1>
        <p class="page-subtitle">查看所有通知和申请消息</p>
      </div>
      <div class="header-actions">
        <t-button variant="outline" @click="markAllAsRead" :disabled="unreadCount === 0">
          <t-icon name="check" style="margin-right: 4px;" />
          全部已读
        </t-button>
        <t-button theme="primary" @click="loadNotifications">
          <t-icon name="refresh" style="margin-right: 4px;" />
          刷新
        </t-button>
      </div>
    </div>

    <div class="card">
      <!-- 筛选标签 -->
      <div class="filter-tabs">
        <t-tabs v-model="activeTab" theme="normal" @change="handleTabChange">
          <t-tab-panel value="all" label="全部">
            <div class="tab-content">
              <div class="notifications-list">
                <div v-if="loading" class="loading-container">
                  <t-loading :loading="loading" text="加载中..." />
                </div>
                <div v-else-if="filteredNotifications.length === 0" class="empty-container">
                  <t-empty description="暂无通知" />
                </div>
                <div v-else>
                  <div
                    v-for="(notif, index) in filteredNotifications"
                    :key="index"
                    class="notification-card"
                    :class="{ unread: !notif.read }"
                    @click="handleNotificationClick(notif)"
                  >
                    <div class="notification-icon-wrapper">
                      <div class="notification-icon" :class="getIconClass(notif.type)">
                        <t-icon :name="notif.icon || 'info-circle'" size="20" />
                      </div>
                      <div v-if="!notif.read" class="unread-dot"></div>
                    </div>
                    <div class="notification-content-wrapper">
                      <div class="notification-header-row">
                        <div class="notification-title">{{ notif.title }}</div>
                        <div class="notification-time">{{ formatTime(notif.time) }}</div>
                      </div>
                      <div class="notification-message">{{ notif.message }}</div>
                      <div v-if="notif.remark" class="notification-remark">
                        <t-icon name="info-circle" size="14" style="margin-right: 4px;" />
                        {{ notif.remark }}
                      </div>
                    </div>
                    <div class="notification-actions" v-if="notif.type === 'member_join_request'">
                      <t-button
                        theme="primary"
                        size="small"
                        @click.stop="handleApproveRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        {{ processingApplication === notif.applicationId ? '处理中...' : '通过' }}
                      </t-button>
                      <t-button
                        variant="outline"
                        theme="danger"
                        size="small"
                        @click.stop="handleRejectRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        拒绝
                      </t-button>
                    </div>
                    <div class="notification-actions" v-else>
                      <t-button
                        v-if="!notif.read"
                        variant="text"
                        size="small"
                        @click.stop="markAsRead(notif)"
                      >
                        标记已读
                      </t-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </t-tab-panel>
          <t-tab-panel value="unread" :label="`未读 (${unreadCount})`">
            <div class="tab-content">
              <div class="notifications-list">
                <div v-if="loading" class="loading-container">
                  <t-loading :loading="loading" text="加载中..." />
                </div>
                <div v-else-if="unreadNotifications.length === 0" class="empty-container">
                  <t-empty description="暂无未读通知" />
                </div>
                <div v-else>
                  <div
                    v-for="(notif, index) in unreadNotifications"
                    :key="index"
                    class="notification-card unread"
                    @click="handleNotificationClick(notif)"
                  >
                    <div class="notification-icon-wrapper">
                      <div class="notification-icon" :class="getIconClass(notif.type)">
                        <t-icon :name="notif.icon || 'info-circle'" size="20" />
                      </div>
                      <div class="unread-dot"></div>
                    </div>
                    <div class="notification-content-wrapper">
                      <div class="notification-header-row">
                        <div class="notification-title">{{ notif.title }}</div>
                        <div class="notification-time">{{ formatTime(notif.time) }}</div>
                      </div>
                      <div class="notification-message">{{ notif.message }}</div>
                      <div v-if="notif.remark" class="notification-remark">
                        <t-icon name="info-circle" size="14" style="margin-right: 4px;" />
                        {{ notif.remark }}
                      </div>
                    </div>
                    <div class="notification-actions" v-if="notif.type === 'member_join_request'">
                      <t-button
                        theme="primary"
                        size="small"
                        @click.stop="handleApproveRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        {{ processingApplication === notif.applicationId ? '处理中...' : '通过' }}
                      </t-button>
                      <t-button
                        variant="outline"
                        theme="danger"
                        size="small"
                        @click.stop="handleRejectRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        拒绝
                      </t-button>
                    </div>
                    <div class="notification-actions" v-else>
                      <t-button
                        variant="text"
                        size="small"
                        @click.stop="markAsRead(notif)"
                      >
                        标记已读
                      </t-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </t-tab-panel>
          <t-tab-panel value="applications" :label="`申请通知 (${memberJoinRequestNotifications.length})`">
            <div class="tab-content">
              <div class="notifications-list">
                <div v-if="loading" class="loading-container">
                  <t-loading :loading="loading" text="加载中..." />
                </div>
                <div v-else-if="memberJoinRequestNotifications.length === 0" class="empty-container">
                  <t-empty description="暂无申请通知" />
                </div>
                <div v-else>
                  <div
                    v-for="(notif, index) in memberJoinRequestNotifications"
                    :key="index"
                    class="notification-card"
                    :class="{ unread: !notif.read }"
                  >
                    <div class="notification-icon-wrapper">
                      <div class="notification-icon" :class="getIconClass(notif.type)">
                        <t-icon :name="notif.icon || 'info-circle'" size="20" />
                      </div>
                      <div v-if="!notif.read" class="unread-dot"></div>
                    </div>
                    <div class="notification-content-wrapper">
                      <div class="notification-header-row">
                        <div class="notification-title">{{ notif.title }}</div>
                        <div class="notification-time">{{ formatTime(notif.time) }}</div>
                      </div>
                      <div class="notification-message">{{ notif.message }}</div>
                      <div v-if="notif.remark" class="notification-remark">
                        <t-icon name="info-circle" size="14" style="margin-right: 4px;" />
                        申请备注：{{ notif.remark }}
                      </div>
                    </div>
                    <div class="notification-actions" v-if="notif.type === 'member_join_request'">
                      <t-button
                        theme="primary"
                        size="small"
                        @click.stop="handleApproveRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        {{ processingApplication === notif.applicationId ? '处理中...' : '通过' }}
                      </t-button>
                      <t-button
                        variant="outline"
                        theme="danger"
                        size="small"
                        @click.stop="handleRejectRequest(notif)"
                        :disabled="processingApplication === notif.applicationId"
                      >
                        拒绝
                      </t-button>
                    </div>
                    <div class="notification-actions" v-else>
                      <t-button
                        v-if="!notif.read"
                        variant="text"
                        size="small"
                        @click.stop="markAsRead(notif)"
                      >
                        标记已读
                      </t-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </t-tab-panel>
        </t-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { teamService } from '@/services/teamService'
import { notificationService } from '@/services/notificationService'
import { useAuthStore } from '@/stores/authStore'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const notifications = ref([])
const loading = ref(false)
const activeTab = ref('all')
const processingApplication = ref(null)

// 计算属性
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

const filteredNotifications = computed(() => {
  return notifications.value.sort((a, b) => new Date(b.time) - new Date(a.time))
})

const unreadNotifications = computed(() => {
  return notifications.value.filter(n => !n.read).sort((a, b) => new Date(b.time) - new Date(a.time))
})

const applicationNotifications = computed(() => {
  return notifications.value.filter(n => 
    n.type === 'application' || n.type === 'member_join_request'
  ).sort((a, b) => new Date(b.time) - new Date(a.time))
})

const memberJoinRequestNotifications = computed(() => {
  return notifications.value.filter(n => n.type === 'member_join_request')
    .sort((a, b) => new Date(b.time) - new Date(a.time))
})

// 加载通知
const loadNotifications = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const userId = authStore.user?.userID || authStore.user?.userId
    if (!userId) {
      loading.value = false
      return
    }

    const newNotifications = []
    
    // 1. 获取我的申请状态更新通知
    try {
      const myApplications = await teamService.getMyApplications(0, 50)
      if (myApplications.applications && Array.isArray(myApplications.applications)) {
        myApplications.applications.forEach(app => {
          if (app.status === 'APPROVED') {
            newNotifications.push({
              id: `app-${app.applicationID || app.id}`,
              title: '申请已通过',
              message: `您的加入团队申请已通过`,
              time: app.processTime || app.applyTime || new Date(),
              read: false,
              icon: 'check-circle',
              type: 'application',
              applicationId: app.applicationID || app.id,
              remark: app.remark || ''
            })
          } else if (app.status === 'REJECTED') {
            newNotifications.push({
              id: `app-${app.applicationID || app.id}`,
              title: '申请已拒绝',
              message: `您的加入团队申请已被拒绝`,
              time: app.processTime || app.applyTime || new Date(),
              read: false,
              icon: 'close-circle',
              type: 'application',
              applicationId: app.applicationID || app.id,
              remark: app.remark || ''
            })
          }
        })
      }
    } catch (error) {
      console.error('加载申请通知失败:', error)
    }
    
    // 2. 获取Flag提交通知
    try {
      const flagNotifications = await notificationService.getFlagSubmissionNotifications(userId)
      newNotifications.push(...flagNotifications)
    } catch (error) {
      console.error('加载Flag提交通知失败:', error)
    }
    
    // 3. 获取Hint更新通知
    try {
      const hintNotifications = await notificationService.getHintUpdateNotifications(userId)
      newNotifications.push(...hintNotifications)
    } catch (error) {
      console.error('加载Hint更新通知失败:', error)
    }
    
    // 4. 获取队伍审核通过通知
    try {
      const auditNotifications = await notificationService.getTeamAuditNotifications(userId)
      newNotifications.push(...auditNotifications)
    } catch (error) {
      console.error('加载队伍审核通知失败:', error)
    }
    
    // 5. 获取成员加入请求通知（作为队长）
    try {
      const joinRequestNotifications = await notificationService.getMemberJoinRequestNotifications(userId)
      newNotifications.push(...joinRequestNotifications)
    } catch (error) {
      console.error('加载成员加入请求通知失败:', error)
    }
    
    // 从localStorage恢复已读状态
    const readStatus = loadReadStatus()
    newNotifications.forEach(notif => {
      if (notif.id && readStatus[notif.id]) {
        notif.read = true
      }
    })
    
    notifications.value = newNotifications.sort((a, b) => new Date(b.time) - new Date(a.time))
  } catch (error) {
    console.error('加载通知失败:', error)
    MessagePlugin.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 从localStorage加载已读状态
const loadReadStatus = () => {
  try {
    const userId = authStore.user?.userID || authStore.user?.userId
    if (!userId) return {}
    const stored = localStorage.getItem(`notification_read_${userId}`)
    return stored ? JSON.parse(stored) : {}
  } catch (error) {
    console.error('加载已读状态失败:', error)
    return {}
  }
}

// 保存已读状态到localStorage
const saveReadStatus = () => {
  try {
    const userId = authStore.user?.userID || authStore.user?.userId
    if (!userId) return
    const readStatus = {}
    notifications.value.forEach(notif => {
      if (notif.read && notif.id) {
        readStatus[notif.id] = true
      }
    })
    localStorage.setItem(`notification_read_${userId}`, JSON.stringify(readStatus))
  } catch (error) {
    console.error('保存已读状态失败:', error)
  }
}

// 标记为已读
const markAsRead = (notif) => {
  notif.read = true
  saveReadStatus()
  MessagePlugin.success('已标记为已读')
}

// 全部已读
const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  saveReadStatus()
  MessagePlugin.success('已标记全部为已读')
}

// 标签页切换
const handleTabChange = (value) => {
  activeTab.value = value
}

// 处理通知点击
const handleNotificationClick = (notif) => {
  // 标记为已读
  if (!notif.read) {
    notif.read = true
    saveReadStatus()
  }
  
  // 根据通知类型跳转
  if (notif.type === 'application' || notif.type === 'team_audit') {
    router.push('/teams')
  } else if (notif.type === 'flag_submission' && notif.challengeId) {
    // 跳转到题目详情或竞赛页面
    router.push('/problems')
  }
}

// 处理通过申请
const handleApproveRequest = async (notif) => {
  if (!notif.applicationId) return
  
  processingApplication.value = notif.applicationId
  try {
    await teamService.processApplication(notif.applicationId, true, '已通过申请')
    MessagePlugin.success('已通过申请')
    notif.read = true
    saveReadStatus()
    // 从通知列表中移除（或更新状态）
    const index = notifications.value.findIndex(n => n.id === notif.id)
    if (index !== -1) {
      notifications.value[index] = {
        ...notif,
        read: true,
        title: '申请已处理',
        message: `您已通过 ${notif.applicantName} 的加入申请`
      }
    }
    // 重新加载通知以获取最新状态
    setTimeout(() => {
      loadNotifications()
    }, 500)
  } catch (error) {
    MessagePlugin.error('处理申请失败: ' + (error.message || '未知错误'))
  } finally {
    processingApplication.value = null
  }
}

// 处理拒绝申请
const handleRejectRequest = async (notif) => {
  if (!notif.applicationId) return
  
  const result = await DialogPlugin.confirm({
    header: '确认拒绝',
    body: `确定要拒绝 ${notif.applicantName} 的加入申请吗？`,
    confirmBtn: { theme: 'danger' }
  })
  
  if (!result) return
  
  processingApplication.value = notif.applicationId
  try {
    await teamService.processApplication(notif.applicationId, false, '已拒绝申请')
    MessagePlugin.success('已拒绝申请')
    notif.read = true
    saveReadStatus()
    // 从通知列表中移除（或更新状态）
    const index = notifications.value.findIndex(n => n.id === notif.id)
    if (index !== -1) {
      notifications.value[index] = {
        ...notif,
        read: true,
        title: '申请已处理',
        message: `您已拒绝 ${notif.applicantName} 的加入申请`
      }
    }
    // 重新加载通知以获取最新状态
    setTimeout(() => {
      loadNotifications()
    }, 500)
  } catch (error) {
    MessagePlugin.error('处理申请失败: ' + (error.message || '未知错误'))
  } finally {
    processingApplication.value = null
  }
}

// 获取图标样式类
const getIconClass = (type) => {
  switch (type) {
    case 'application':
      return 'icon-application'
    case 'flag_submission':
      return 'icon-flag'
    case 'hint_update':
      return 'icon-hint'
    case 'team_audit':
      return 'icon-team'
    case 'member_join_request':
      return 'icon-member-request'
    default:
      return 'icon-default'
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const diff = now - new Date(time)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  animation: fadeIn 0.3s ease-in-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.notification-card {
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.notification-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.notification-card.unread {
  background: rgba(59, 130, 246, 0.05);
  border-left: 3px solid #3b82f6;
}

.notification-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.notification-icon.icon-application {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.notification-icon.icon-flag {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.notification-icon.icon-hint {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.notification-icon.icon-team {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.notification-icon.icon-member-request {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.notification-icon.icon-default {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
}

.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid #1a1f3a;
}

.notification-content-wrapper {
  flex: 1;
  min-width: 0;
}

.notification-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #e0e6ed;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  margin-left: 12px;
}

.notification-message {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.6;
  margin-bottom: 8px;
}

.notification-remark {
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.notification-actions {
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
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
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: stretch;
  }

  .header-actions > * {
    flex: 1;
  }

  .notification-card {
    flex-direction: column;
  }

  .notification-actions {
    align-self: flex-end;
  }
}
</style>

