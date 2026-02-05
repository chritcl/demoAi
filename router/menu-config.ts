import type { RouteRecordRaw } from 'vue-router'

// 定义路由配置
export const menuRoutes: RouteRecordRaw[] = [
  // 根路径重定向到登录页
  {
    path: '/',
    redirect: '/login',
  },
  // 登录页
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/not-found',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: {
      requiresAuth: true,
    },
    children: [
      // 领导驾驶舱模块（隐藏路由）
      {
        path: '/systemOverview',
        name: 'systemOverview',
        component: () => import('@/views/dashboard/SystemOverview.vue'),
        meta: {
          title: '区域概览',
          icon: 'Odometer',
          requiresAuth: true,
          hidden: true, // 在菜单中隐藏
          order: 1,
          affix: true, // 标签页固定
        },
      },
      // 个人中心路由
      {
        path: '/personal-information',
        name: 'personal-information',
        component: () => import('@/views/PersonalInformation.vue'),
        meta: {
          title: '个人中心',
          icon: 'User',
          requiresAuth: true,
          hidden: true,
          order: 2,
          affix: true,
        },
      }
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'catch-all',
    component: () => import('@/views/NotFoundView.vue'),
    meta: {
      requiresAuth: false,
      originalPath: true, // 标记这是catch-all路由
    },
  },
]
