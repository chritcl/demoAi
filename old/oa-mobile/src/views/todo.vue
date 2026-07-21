<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="我的待办" fixed />
    <van-tabs v-model:active="active" @change="load" sticky>
      <van-tab title="待办" />
      <van-tab title="已办" />
    </van-tabs>
    <van-cell-group>
      <van-cell v-for="t in list" :key="t.id" :title="t.title" is-link @click="go(t)">
        <template #label>
          <div>{{ bizLabel(t.businessType) }} ｜ {{ t.nodeName }}</div>
          <div class="muted">{{ t.startUserName }} 发起 · {{ fmt(t.actionTime || t.createTime) }}</div>
        </template>
      </van-cell>
    </van-cell-group>
    <van-empty v-if="!list.length" :description="active === 0 ? '暂无待办' : '暂无已办'" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { todoTasks, doneTasks } from '@/api'

const router = useRouter()
const active = ref(0)
const list = ref([])
async function load() {
  list.value = active.value === 0 ? (await todoTasks()).data || [] : (await doneTasks()).data || []
}
function go(t) { if (active.value === 0) router.push('/approve/' + t.id) }
function bizLabel(t) { return { leave: '请假', vehicle: '用车', seal: '用印', trip: '出差', document_send: '发文' }[t] || t }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
