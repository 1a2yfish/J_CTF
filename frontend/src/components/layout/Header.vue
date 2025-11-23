<template>
  <div class="header">
    <div class="header-left">
      <!-- Logo -->
      <div class="header-logo" @click="router.push('/dashboard')">
        <div class="logo-icon">
          <t-icon name="shield" />
        </div>
        <div class="logo-text">
          <div class="logo-title">CTF Platform</div>
        </div>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="header-nav" :class="{ 'mobile-open': showMobileMenu }">
        <div 
          class="nav-item"
          :class="{ active: isActiveRoute('dashboard') }"
          @click="navigate('dashboard')"
        >
          <t-icon name="dashboard" />
          <span>仪表盘</span>
        </div>
        <div 
          class="nav-item"
          :class="{ active: isActiveRoute('competitions') }"
          @click="navigate('competitions')"
        >
          <t-icon name="flag" />
          <span>竞赛列表</span>
        </div>
        <div 
          class="nav-item"
          :class="{ active: isActiveRoute('teams') }"
          @click="navigate('teams')"
        >
          <t-icon name="team" />
          <span>团队管理</span>
        </div>
        <div 
          v-if="isAdmin"
          class="nav-item"
          :class="{ active: isActiveRoute('admin') }"
          @click="navigate('admin')"
        >
          <t-icon name="setting" />
          <span>管理后台</span>
        </div>
        <div 
          class="nav-item notification-nav-item"
          :class="{ active: isActiveRoute('notifications') }"
          @click="navigate('notifications')"
        >
          <div class="notification-icon-wrapper">
            <t-badge v-if="notificationCount > 0" :count="notificationCount" :dot="true">
              <t-icon name="notification" />
            </t-badge>
            <t-icon v-else name="notification" />
          </div>
          <span>通知中心</span>
        </div>
      </nav>
    </div>
    
    <div class="header-right">
      <!-- 用户信息 -->
      <div class="user-info" @click.stop="showProfileMenu = !showProfileMenu">
        <div class="user-avatar">{{ userInitials }}</div>
        <div v-if="!isMobile" class="user-details">
          <div class="user-name">{{ currentUser.userName || currentUser.name || '用户' }}</div>
          <div class="user-role">{{ userRoleLabel }}</div>
        </div>
        <t-icon name="chevron-down" size="16" class="chevron-icon" />
        <div v-if="showProfileMenu" class="user-menu" @click.stop>
          <div class="menu-item" @click="goToProfile">
            <t-icon name="user" />
            <span>个人中心</span>
          </div>
          <div class="menu-item" @click="goToSettings">
            <t-icon name="setting" />
            <span>设置</span>
          </div>
          <div class="menu-divider"></div>
          <div class="menu-item logout-item" @click="handleLogout">
            <t-icon name="power" />
            <span>退出登录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Message } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showProfileMenu = ref(false)
const showMobileMenu = ref(false)
const isMobile = ref(false)

const currentUser = computed(() => {
  return authStore.user || {}
})

const isAdmin = computed(() => {
  return authStore.isAdmin
})

const userInitials = computed(() => {
  const name = currentUser.value.userName || currentUser.value.name || ''
  if (!name) return 'U'
  return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2)
})

const userRoleLabel = computed(() => {
  if (isAdmin.value) return '管理员'
  return '普通用户'
})

// 导航相关
const isActiveRoute = (routeName) => {
  const currentPath = route.path
  if (routeName === 'dashboard') {
    return currentPath === '/dashboard' || currentPath === '/'
  }
  if (routeName === 'admin') {
    return currentPath.startsWith('/admin')
  }
  if (routeName === 'notifications') {
    return currentPath === '/notifications'
  }
  return currentPath.startsWith(`/${routeName}`)
}

const navigate = (routeName) => {
  router.push({ path: `/${routeName}` })
  // 移动端导航后关闭菜单
  if (isMobile.value) {
    showMobileMenu.value = false
  }
}

// 通知相关
const notifications = ref([])
const loadingNotifications = ref(false)

const notificationCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// 加载通知数据
const loadNotifications = async () => {
  if (loadingNotifications.value) return
  loadingNotifications.value = true
  try {
    const { notificationService } = await import('@/services/notificationService')
    const userId = authStore.user?.userID || authStore.user?.userId
    
    if (!userId) {
      loadingNotifications.value = false
      return
    }
    
    const newNotifications = []
    
    // 1. 获取我的申请状态更新通知
    try {
      const { teamService } = await import('@/services/teamService')
      const myApplications = await teamService.getMyApplications(0, 20)
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
              applicationId: app.applicationID || app.id
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
              applicationId: app.applicationID || app.id
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
    
    // 3. 获取队伍审核通过通知
    try {
      const auditNotifications = await notificationService.getTeamAuditNotifications(userId)
      newNotifications.push(...auditNotifications)
    } catch (error) {
      console.error('加载队伍审核通知失败:', error)
    }
    
    // 4. 获取成员加入请求通知（作为队长）
    try {
      const joinRequestNotifications = await notificationService.getMemberJoinRequestNotifications(userId)
      newNotifications.push(...joinRequestNotifications)
    } catch (error) {
      console.error('加载成员加入请求通知失败:', error)
    }
    
    // 从localStorage恢复已读状态
    try {
      const stored = localStorage.getItem(`notification_read_${userId}`)
      if (stored) {
        const readStatus = JSON.parse(stored)
        newNotifications.forEach(notif => {
          if (notif.id && readStatus[notif.id]) {
            notif.read = true
          }
        })
      }
    } catch (error) {
      console.error('恢复已读状态失败:', error)
    }
    
    notifications.value = newNotifications.sort((a, b) => new Date(b.time) - new Date(a.time))
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    loadingNotifications.value = false
  }
}

const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  Message.success('已标记全部为已读')
}


// 跳转到通知页面
const goToNotifications = () => {
  router.push('/notifications')
}


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
  return new Date(time).toLocaleDateString('zh-CN')
}

const goToProfile = () => {
  showProfileMenu.value = false
  router.push('/profile')
}

const goToSettings = () => {
  showProfileMenu.value = false
  Message.info('设置功能开发中')
}

const handleLogout = async () => {
  showProfileMenu.value = false
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('登出失败:', error)
    router.push('/login')
  }
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (!event.target.closest('.user-info')) {
    showProfileMenu.value = false
  }
  if (!event.target.closest('.header-nav') && !event.target.closest('.mobile-menu-btn')) {
    showMobileMenu.value = false
  }
}

let notificationInterval = null

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  document.addEventListener('click', handleClickOutside)
  // 初始加载通知（用于显示未读数量）
  if (authStore.isAuthenticated) {
    loadNotifications()
    // 定期刷新通知（每30秒）
    notificationInterval = setInterval(() => {
      if (authStore.isAuthenticated) {
        loadNotifications()
      }
    }, 30000)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  document.removeEventListener('click', handleClickOutside)
  if (notificationInterval) {
    clearInterval(notificationInterval)
  }
})
</script>

<style scoped>
.header {
  height: 70px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.95) 100%);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
  min-width: 0;
  flex-shrink: 0;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.header-logo:hover {
  background: rgba(59, 130, 246, 0.1);
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 8px;
  color: #fff;
  font-size: 20px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #e0e6ed;
  line-height: 1.2;
  white-space: nowrap;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  position: relative;
}

.nav-item:hover {
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
}

.nav-item.active {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.notification-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.notification-nav-item {
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  margin-left: auto;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 10px;
  transition: all 0.2s;
  border: 1px solid transparent;
  flex-shrink: 0;
}

.user-info:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.2);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #e0e6ed;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.2;
}

.chevron-icon {
  color: #9ca3af;
  transition: transform 0.2s;
}

.user-info:hover .chevron-icon {
  transform: rotate(180deg);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  min-width: 160px;
  z-index: 1000;
  overflow: hidden;
  padding: 4px 0;
}

.user-menu .menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  color: #e0e6ed;
  font-size: 14px;
}

.user-menu .menu-item:hover {
  background: #2a3458;
}

.user-menu .menu-item.logout-item {
  color: #f5222d;
}

.user-menu .menu-item.logout-item:hover {
  background: rgba(245, 34, 45, 0.1);
}

.user-menu .menu-divider {
  height: 1px;
  background: #2a3458;
  margin: 4px 0;
}

.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  margin-right: 12px;
  color: #e0e6ed;
  border-radius: 6px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.mobile-menu-btn:hover {
  background: rgba(59, 130, 246, 0.1);
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .header-nav {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    background: #1a1f3a;
    border-bottom: 1px solid #2a3458;
    flex-direction: column;
    align-items: stretch;
    padding: 8px;
    gap: 4px;
    transform: translateY(-100%);
    opacity: 0;
    transition: all 0.3s ease;
    z-index: 999;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  }

  .header-nav.mobile-open {
    transform: translateY(0);
    opacity: 1;
  }

  .header-nav .nav-item {
    padding: 12px 16px;
    justify-content: flex-start;
  }

  .logo-text {
    display: none;
  }

  .user-details {
    display: none;
  }
}
</style>
