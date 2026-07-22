<script setup lang="ts">
const foundations = [
  { name: '后端服务', description: 'Spring Boot、Flyway 与 ArchUnit 已纳入工程基础', status: '已配置' },
  { name: 'PC 应用', description: 'Vue、路由、状态管理与请求封装可直接承接纵向功能', status: '已配置' },
  { name: '移动 H5', description: 'uni-app H5 工程将与后端 API 保持同一业务边界', status: '待构建' }
];
</script>

<template>
  <section class="workspace-status">
    <el-card shadow="never" class="workspace-status__intro">
      <p class="workspace-status__eyebrow">工程基础</p>
      <h1>协同办公平台</h1>
      <p>当前仅提供工程状态与公共基础，不包含任何业务功能页面。</p>
    </el-card>

    <div class="workspace-status__grid">
      <el-card v-for="foundation in foundations" :key="foundation.name" shadow="never">
        <template #header>
          <div class="workspace-status__card-header">
            <strong>{{ foundation.name }}</strong>
            <el-tag type="success" effect="plain">{{ foundation.status }}</el-tag>
          </div>
        </template>
        <p>{{ foundation.description }}</p>
      </el-card>
    </div>
  </section>
</template>
