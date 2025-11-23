<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <h1 class="page-title">仪表盘</h1>
        <p class="page-subtitle">欢迎回来</p>
      </div>
    </div>

    <!-- 快速导航 -->
    <div class="card">
      <div class="page-title" style="margin: 0 0 20px 0; font-size: 1.2rem;">快速导航</div>
      <div class="quick-nav-grid">
        <div class="nav-card" @click="navigateTo('/competitions')">
          <div class="nav-icon competitions">
            <t-icon name="flag" size="32" />
          </div>
          <div class="nav-title">竞赛列表</div>
          <div class="nav-desc">查看所有竞赛</div>
        </div>
        <div class="nav-card" @click="navigateTo('/teams')">
          <div class="nav-icon teams">
            <t-icon name="team" size="32" />
          </div>
          <div class="nav-title">团队管理</div>
          <div class="nav-desc">管理我的团队</div>
        </div>
        <div class="nav-card" @click="navigateTo('/profile')">
          <div class="nav-icon profile">
            <t-icon name="user" size="32" />
          </div>
          <div class="nav-title">个人中心</div>
          <div class="nav-desc">查看个人信息</div>
        </div>
        <div class="nav-card" @click="navigateTo('/competitions')">
          <div class="nav-icon challenges">
            <t-icon name="file-code" size="32" />
          </div>
          <div class="nav-title">挑战题目</div>
          <div class="nav-desc">查看所有挑战</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>