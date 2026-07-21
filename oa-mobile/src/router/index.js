import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login.vue') },
  {
    path: '/',
    component: () => import('@/views/layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home.vue'), meta: { tab: 'home' } },
      { path: 'todo', name: 'Todo', component: () => import('@/views/todo.vue'), meta: { tab: 'todo' } },
      { path: 'contacts', name: 'Contacts', component: () => import('@/views/contacts.vue'), meta: { tab: 'contacts' } },
      { path: 'mine', name: 'Mine', component: () => import('@/views/mine.vue'), meta: { tab: 'mine' } }
    ]
  },
  { path: '/approve/:id', name: 'Approve', component: () => import('@/views/approve.vue'), props: true },
  { path: '/notice/:id', name: 'Notice', component: () => import('@/views/notice.vue'), props: true },
  { path: '/message', name: 'Message', component: () => import('@/views/message.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })

export default router
