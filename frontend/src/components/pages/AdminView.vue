<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">管理后台</h1>
        <p class="page-subtitle">管理系统用户、竞赛、团队和题目</p>
      </div>
    </div>

    <div class="content">
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

      <!-- 管理功能入口卡片 -->
      <div class="management-section">
        <h3 class="section-title">管理功能</h3>
        <div class="management-grid">
          <div class="management-card" @click="navigateTo('/admin/competitions')">
            <div class="management-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
              <t-icon name="flag" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">竞赛管理</div>
              <div class="management-desc">创建、编辑和管理竞赛</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>

          <div class="management-card" @click="navigateTo('/admin/competitions/audit')">
            <div class="management-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
              <t-icon name="check-circle" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">竞赛审核</div>
              <div class="management-desc">审核待审核的竞赛申请</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>

          <div class="management-card" @click="navigateTo('/admin/teams')">
            <div class="management-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;">
              <t-icon name="team" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">团队管理</div>
              <div class="management-desc">查看和管理所有团队</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>

          <div class="management-card" @click="navigateTo('/admin/teams/audit')">
            <div class="management-icon" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
              <t-icon name="file-check" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">团队审核</div>
              <div class="management-desc">审核待审核的团队申请</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>

          <div class="management-card" @click="navigateTo('/admin/users')">
            <div class="management-icon" style="background: rgba(59, 130, 246, 0.1); color: #3b82f6;">
              <t-icon name="user" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">用户管理</div>
              <div class="management-desc">管理系统用户信息</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>

          <div class="management-card" @click="navigateTo('/admin/flag-submissions')">
            <div class="management-icon" style="background: rgba(236, 72, 153, 0.1); color: #ec4899;">
              <t-icon name="file-code" size="32" />
            </div>
            <div class="management-content">
              <div class="management-title">Flag提交</div>
              <div class="management-desc">查看和管理Flag提交记录</div>
            </div>
            <div class="management-arrow">
              <t-icon name="chevron-right" size="20" />
            </div>
          </div>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminService } from '@/services/adminService'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

// 统计数据
const stats = ref({
  totalUsers: 0,
  totalCompetitions: 0,
  totalTeams: 0,
  totalChallenges: 0
})


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



// 导航到管理页面
const navigateTo = (path) => {
  router.push(path)
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

.content {
  padding: 24px 0;
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

.management-section {
  margin-top: 32px;
}

.management-section .section-title {
  font-size: 20px;
  font-weight: 600;
  color: #e0e6ed;
  margin-bottom: 20px;
}

.management-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.management-card {
  background: #1a1f3a;
  border: 1px solid #2a3458;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.management-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.management-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

.management-card:hover::before {
  transform: scaleY(1);
}

.management-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.management-card:hover .management-icon {
  transform: scale(1.1);
}

.management-content {
  flex: 1;
  min-width: 0;
}

.management-title {
  font-size: 18px;
  font-weight: 600;
  color: #e0e6ed;
  margin-bottom: 4px;
}

.management-desc {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.4;
}

.management-arrow {
  color: #9ca3af;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.management-card:hover .management-arrow {
  color: #3b82f6;
  transform: translateX(4px);
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

