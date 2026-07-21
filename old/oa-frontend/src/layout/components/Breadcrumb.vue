<template>
  <el-breadcrumb class="breadcrumb" separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, idx) in levelList" :key="idx">{{ item }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const levelList = computed(() => {
  const matched = route.matched.filter((r) => r.meta && r.meta.title)
  return matched.map((r) => r.meta.title)
})
</script>

<style scoped>
.breadcrumb { display: inline-flex; }
</style>
