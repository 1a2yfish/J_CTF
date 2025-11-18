import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

import App from './App.vue'
import DefaultLayout from './components/layout/DefaultLayout.vue'
import LoginView from './components/auth/LoginView.vue'
import RegisterView from './components/auth/RegisterView.vue'
import DashboardView from './components/views/DashboardView.vue'
import CompetitionsView from './components/views/CompetitionsView.vue'
import CompetitionDetailView from './components/views/CompetitionDetailView.vue'
import TeamsView from './components/views/TeamsView.vue'
import ProfileView from './components/views/ProfileView.vue'

import 'tdesign-vue-next/es/style/index.css'
import './assets/styles/base.css'
import {useAuthStore} from "@/stores/authStore";

const app = createApp(App)

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
            component: DashboardView,
            meta: { requiresAuth: true }
        },
        {
            path: '/competitions',
            component: CompetitionsView,
            meta: { requiresAuth: true }
        },
        {
            path: '/competitions/:id',
            component: CompetitionDetailView,
            meta: { requiresAuth: true }
        },
        {
            path: '/teams',
            component: TeamsView,
            meta: { requiresAuth: true }
        },
        {
            path: '/profile',
            component: ProfileView,
            meta: { requiresAuth: true }
        }
    ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
        next('/dashboard')
    } else {
        next()
    }
})

app.use(router)
app.mount('#app')
