<template>
  <div class="sidebar">
    <div class="logo">
      <img src="@/assets/logo.svg" class="logo-img" alt="logo" />
      <span v-show="!collapsed" class="logo-text">协同办公平台</span>
    </div>
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path + (route.name || '')"
          :item="route"
          base-path=""
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/store/permission'
import { useAppStore } from '@/store/app'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const permStore = usePermissionStore()
const appStore = useAppStore()

const routes = computed(() => permStore.sidebarRoutes)
const collapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.sidebar {
  height: 100vh;
  background: #304156;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background: #2b3a4d;
  overflow: hidden;
  .logo-img { width: 28px; height: 28px; }
  .logo-text { font-size: 16px; font-weight: 600; white-space: nowrap; }
}
.el-menu { border-right: none; }
:deep(.el-scrollbar) { flex: 1; }
</style>
