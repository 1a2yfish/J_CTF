import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

import App from './App.vue'
import DefaultLayout from './components/layout/DefaultLayout.vue'
import LoginView from './components/auth/LoginView.vue'
import RegisterView from './components/auth/RegisterView.vue'
import DashboardView from './components/pages/DashboardView.vue'
import CompetitionsView from './components/pages/CompetitionsView.vue'
import CompetitionDetailView from './components/pages/CompetitionDetailView.vue'
import TeamsView from './components/pages/TeamsView.vue'
import ProfileView from './components/pages/ProfileView.vue'
import AdminView from './components/pages/AdminView.vue'
import ProblemsView from './components/pages/ProblemsView.vue'
import NotificationsView from './components/pages/NotificationsView.vue'
import LeaderboardView from './components/pages/LeaderboardView.vue'

import 'tdesign-vue-next/es/style/index.css'
import './assets/styles/base.css'
import './assets/styles/theme.css'
import {useAuthStore} from "@/stores/authStore";
import TDesign from 'tdesign-vue-next'

const app = createApp(App)

// 注册 TDesign 组件库
app.use(TDesign)

// 创建Pinia
const pinia = createPinia()
app.use(pinia)

// 创建路由
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/dashboard'
        },
        {
            path: '/login',
            component: LoginView
        },
        {
            path: '/register',
            component: RegisterView
        },
        {
            path: '/dashboard',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: DashboardView
                }
            ]
        },
        {
            path: '/competitions',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: CompetitionsView
                }
            ]
        },
        {
            path: '/competitions/:id',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: CompetitionDetailView
                }
            ]
        },
        {
            path: '/teams',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: TeamsView
                }
            ]
        },
        {
            path: '/problems',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: ProblemsView
                }
            ]
        },
        {
            path: '/leaderboard',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: LeaderboardView
                }
            ]
        },
        {
            path: '/notifications',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: NotificationsView
                }
            ]
        },
        {
            path: '/profile',
            component: DefaultLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    component: ProfileView
                }
            ]
        },
        {
            path: '/admin',
            component: DefaultLayout,
            meta: { requiresAuth: true, requiresAdmin: true },
            children: [
                {
                    path: '',
                    component: AdminView
                }
            ]
        }
    ]
})

// 路由守卫 - 必须在app.use(pinia)之后
router.beforeEach((to, from, next) => {
    // 在路由守卫中获取store
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
        next('/dashboard')
    } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
        next('/dashboard')
    } else {
        next()
    }
})

app.use(router)
app.mount('#app')
