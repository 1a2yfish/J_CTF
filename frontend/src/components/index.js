/**
 * 组件统一导出文件
 * 参考 GZCTF 的组件组织方式
 */

// Layout 组件
export { default as DefaultLayout } from './layout/DefaultLayout.vue'
export { default as Header } from './layout/Header.vue'
export { default as Sidebar } from './layout/Sidebar.vue'

// Pages 组件
export { default as DashboardView } from './pages/DashboardView.vue'
export { default as CompetitionsView } from './pages/CompetitionsView.vue'
export { default as CompetitionDetailView } from './pages/CompetitionDetailView.vue'
export { default as TeamsView } from './pages/TeamsView.vue'
export { default as ProfileView } from './pages/ProfileView.vue'

// Auth 组件
export { default as LoginView } from './auth/LoginView.vue'
export { default as RegisterView } from './auth/RegisterView.vue'

// Competition 组件
export { default as CompetitionCard } from './competition/CompetitionCard.vue'

// Challenge 组件
export { default as ProblemCard } from './challenge/ProblemCard.vue'
export { default as FlagSubmission } from './challenge/FlagSubmission.vue'

// Team 组件
export { default as TeamCard } from './team/TeamCard.vue'

// Common 组件
export { default as StatCard } from './common/StatCard.vue'

// Modal 组件
export { default as CreateCompetitionModal } from './modals/CreateCompetitionModal.vue'
export { default as JoinTeamModal } from './modals/JoinTeamModal.vue'

