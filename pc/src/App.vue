<script setup lang="ts">
import { RouterView } from 'vue-router';
import { useAppStore } from '@/stores/app';

const appStore = useAppStore();
</script>

<template>
  <el-container class="application-shell">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="application-shell__aside">
      <div class="application-shell__brand">{{ appStore.sidebarCollapsed ? 'OA' : '协同办公' }}</div>
      <el-menu router default-active="/" :collapse="appStore.sidebarCollapsed">
        <el-menu-item index="/">工程状态</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="application-shell__header">
        <el-button text @click="appStore.toggleSidebar">切换导航</el-button>
        <span>开发基础</span>
      </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
