<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <keep-alive>
        <component :is="Component" :key="route.fullPath" />
      </keep-alive>
    </router-view>
  </section>
</template>

<script setup></script>

<style scoped>
.app-main {
  min-height: calc(100vh - 50px);
  background: #f0f2f5;
}
</style>
