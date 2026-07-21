<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="公告详情" left-arrow @click-left="$router.back()" fixed />
    <div class="box" v-if="data.title">
      <h2>{{ data.title }}</h2>
      <div class="muted">{{ data.publishUserName }} · {{ fmt(data.publishTime) }} · 阅读 {{ data.readCount }}</div>
      <div class="content" v-html="data.content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { getNotice } from '@/api'

const route = useRoute()
const data = ref({})
getNotice(route.params.id).then((res) => (data.value = res.data || {}))
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
</script>

<style scoped>
.box { background: #fff; padding: 16px; }
h2 { margin: 0 0 8px; font-size: 18px; }
.content { margin-top: 12px; line-height: 1.7; }
</style>
