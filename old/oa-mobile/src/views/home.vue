<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="协同办公" fixed />
    <div class="user-bar">
      <van-image round width="48" height="48" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" />
      <div style="margin-left:12px">
        <div class="m-title">{{ user.nickname || '用户' }}</div>
        <div class="muted">欢迎回到协同办公平台</div>
      </div>
    </div>

    <van-grid :column-num="4" :border="false" class="grid">
      <van-grid-item v-for="g in grid" :key="g.text" :icon="g.icon" :text="g.text" @click="g.go && g.go()" />
    </van-grid>

    <div class="sec">
      <div class="sec-title">数据概览</div>
      <van-grid :column-num="3" :border="false">
        <van-grid-item><div class="num">{{ s.todoCount || 0 }}</div><div class="muted">待办</div></van-grid-item>
        <van-grid-item><div class="num">{{ s.doneCount || 0 }}</div><div class="muted">已办</div></van-grid-item>
        <van-grid-item><div class="num">{{ s.unreadMessage || 0 }}</div><div class="muted">未读消息</div></van-grid-item>
      </van-grid>
    </div>

    <div class="sec">
      <div class="sec-title">最新公告</div>
      <van-cell v-for="n in notices" :key="n.id" :title="n.title" is-link @click="toNotice(n.id)">
        <template #label>{{ fmt(n.publishTime) }}</template>
      </van-cell>
      <van-empty v-if="!notices.length" description="暂无公告" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { summary, publishedNotice } from '@/api'

const router = useRouter()
const user = useUserStore()
const s = ref({})
const notices = ref([])
const grid = [
  { icon: 'todo-list-o', text: '待办', go: () => router.push('/todo') },
  { icon: 'envelop-o', text: '消息', go: () => router.push('/message') },
  { icon: 'contact', text: '通讯录', go: () => router.push('/contacts') },
  { icon: 'newspaper-o', text: '公告', go: () => router.push('/message') }
]
function toNotice(id) { router.push('/notice/' + id) }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(async () => {
  s.value = (await summary()).data || {}
  notices.value = ((await publishedNotice({ pageNum: 1, pageSize: 5 })).data || {}).list || []
})
</script>

<style scoped>
.user-bar { display: flex; align-items: center; padding: 16px; background: #fff; }
.grid { margin: 8px 0; }
.sec { margin: 8px; background: #fff; border-radius: 8px; overflow: hidden; }
.sec-title { padding: 12px 14px 4px; font-weight: 600; }
.num { font-size: 22px; font-weight: 700; color: #1989fa; }
</style>
