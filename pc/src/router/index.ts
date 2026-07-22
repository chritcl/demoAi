import { createRouter, createWebHistory } from 'vue-router';
import WorkspaceStatusView from '@/views/WorkspaceStatusView.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'workspace-status',
      component: WorkspaceStatusView
    }
  ]
});
