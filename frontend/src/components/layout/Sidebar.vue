<template>
  <div class="sidebar" :class="{ collapsed: collapsed }">
    <div class="sidebar-logo" @click="collapsed ? emit('toggle') : null">
      <div class="logo-icon">
        <t-icon name="shield" />
      </div>
      <div v-if="!collapsed" class="logo-text">
        <div class="logo-title">CTF Platform</div>
        <div class="logo-subtitle">竞赛平台</div>
      </div>
    </div>

    <div class="sidebar-menu">
      <!-- 主要功能 -->
      <div class="menu-section">
        <div
            class="menu-item"
            :class="{ active: isActiveRoute('dashboard') }"
            @click="navigate('dashboard')"
            :title="collapsed ? '仪表盘' : ''"
        >
          <div class="menu-icon">
            <t-icon name="dashboard" />
          </div>
          <span v-if="!collapsed" class="menu-text">仪表盘</span>
        </div>

        <div
            class="menu-item"
            :class="{ active: isActiveRoute('competitions') }"
            @click="navigate('competitions')"
            :title="collapsed ? '竞赛列表' : ''"
        >
          <div class="menu-icon">
            <t-icon name="flag" />
          </div>
          <span v-if="!collapsed" class="menu-text">竞赛列表</span>
        </div>

        <div
            class="menu-item"
            :class="{ active: isActiveRoute('teams') }"
            @click="navigate('teams')"
            :title="collapsed ? '团队管理' : ''"
        >
          <div class="menu-icon">
            <t-icon name="team" />
          </div>
          <span v-if="!collapsed" class="menu-text">团队管理</span>
        </div>
      </div>

      <div class="menu-divider"></div>

      <!-- 用户相关 -->
      <div class="menu-section">
        <div
            class="menu-item"
            :class="{ active: isActiveRoute('profile') }"
            @click="navigate('profile')"
            :title="collapsed ? '个人中心' : ''"
        >
          <div class="menu-icon">
            <t-icon name="user" />
          </div>
          <span v-if="!collapsed" class="menu-text">个人中心</span>
        </div>

        <div
            v-if="isAdmin"
            class="menu-item"
            :class="{ active: isActiveRoute('admin') }"
            @click="navigate('admin')"
            :title="collapsed ? '管理后台' : ''"
        >
          <div class="menu-icon">
            <t-icon name="setting" />
          </div>
          <span v-if="!collapsed" class="menu-text">管理后台</span>
        </div>
      </div>

      <div class="menu-divider"></div>

      <!-- 底部操作 -->
      <div class="menu-section menu-section-bottom">
        <div
            class="menu-item"
            @click="emit('toggle')"
            :title="collapsed ? '展开' : '折叠'"
        >
          <div class="menu-icon">
            <t-icon :name="collapsed ? 'chevron-right' : 'chevron-left'" />
          </div>
          <span v-if="!collapsed" class="menu-text">折叠菜单</span>
        </div>

        <div
            class="menu-item logout-item"
            @click="logout"
            :title="collapsed ? '退出登录' : ''"
        >
          <div class="menu-icon">
            <t-icon name="power" />
          </div>
          <span v-if="!collapsed" class="menu-text">退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle'])

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isAdmin = computed(() => {
  return authStore.isAdmin
})

const isActiveRoute = (routeName) => {
  const currentPath = route.path
  if (routeName === 'dashboard') {
    return currentPath === '/dashboard' || currentPath === '/'
  }
  if (routeName === 'admin') {
    return currentPath.startsWith('/admin')
  }
  return currentPath.startsWith(`/${routeName}`)
}

const navigate = (routeName) => {
  router.push({ path: `/${routeName}` })
}

const logout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('登出失败:', error)
    router.push('/login')
  }
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 260px;
  height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  border-right: 1px solid rgba(59, 130, 246, 0.1);
  backdrop-filter: blur(10px);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-logo {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.05) 100%);
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.sidebar.collapsed .sidebar-logo {
  padding: 0;
  justify-content: center;
}

.sidebar-logo:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.1) 100%);
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 10px;
  color: #fff;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.logo-text {
  flex: 1;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #e0e6ed;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 11px;
  color: #9ca3af;
  line-height: 1.2;
  margin-top: 2px;
}

.sidebar-menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.menu-section {
  flex: 0 0 auto;
}

.menu-section-bottom {
  margin-top: auto;
  padding-top: 16px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  gap: 12px;
  margin: 2px 12px;
  border-radius: 10px;
  position: relative;
  white-space: nowrap;
}

.sidebar.collapsed .menu-item {
  padding: 12px;
  justify-content: center;
  margin: 2px 8px;
}

.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover {
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
  transform: translateX(4px);
}

.menu-item.active {
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.2) 0%, rgba(37, 99, 235, 0.1) 100%);
  color: #60a5fa;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.menu-item.active::before {
  height: 60%;
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.menu-text {
  font-size: 14px;
  font-weight: 500;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.sidebar.collapsed .menu-text {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.menu-item.logout-item {
  color: #ef4444;
}

.menu-item.logout-item:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.menu-item span {
  font-size: 14px;
}

.menu-divider {
  height: 1px;
  background: rgba(59, 130, 246, 0.1);
  margin: 12px 20px;
  transition: margin 0.3s ease;
}

.sidebar.collapsed .menu-divider {
  margin: 12px 16px;
}

/* 滚动条美化 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: rgba(59, 130, 246, 0.05);
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.3);
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .sidebar.collapsed {
    transform: translateX(-100%);
  }
}
</style>
