<template>
  <div class="layout-wrapper">
    <div class="layout-sidebar" :class="{ collapsed }">
      <Sidebar />
    </div>
    <div class="layout-main">
      <Navbar ref="navbarRef" />
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAppStore } from '@/store/app'
import Sidebar from './components/Sidebar/index.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'

const appStore = useAppStore()
const collapsed = computed(() => appStore.sidebarCollapsed)
const navbarRef = ref()
</script>

<style scoped lang="scss">
.layout-wrapper { display: flex; height: 100vh; }
.layout-sidebar { width: 220px; flex-shrink: 0; transition: width 0.28s; }
.layout-sidebar.collapsed { width: 64px; }
.layout-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
</style>
