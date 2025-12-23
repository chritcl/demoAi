import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  // {
  //   path: '/',
  //   name: 'home',
  //   component: HomeView
  // },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/index',
    children: [
      {
        path: '/index',
        name: '首页',
        component: () => import('../views/HomeView.vue')
      },
      {
        path: '/usercenter',
        name: '个人中心',
        component: () => import('../views/syspage/personcenter/index.vue')
      }
    ]
  },
  {
    path: '/login',
    name: '登录',
    component: () => import('../views/login/LoginPage.vue')
  },
  {
    path: "/:pathMatch(.*)",
    name: "NotFound",
    component: () => import("../views/login/LoginPage.vue"),
  }
]

const router = createRouter({
  // history: createWebHistory(process.env.BASE_URL),
  history: createWebHashHistory(),
  routes
})

export default router
