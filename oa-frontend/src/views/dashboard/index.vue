<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :sm="12" :md="6" v-for="c in cards" :key="c.key">
        <div class="stat-card" :style="{ background: c.bg }">
          <el-icon class="stat-icon"><component :is="c.icon" /></el-icon>
          <div class="stat-body">
            <div class="stat-num">{{ summary[c.key] ?? 0 }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :md="14">
        <el-card shadow="never" header="最近公告">
          <el-table :data="summary.recentNotices || []" :show-header="false" size="large">
            <el-table-column>
              <template #default="{ row }">
                <el-tag size="small" type="danger" v-if="row.top">置顶</el-tag>
                <span class="notice-title" @click="$router.push('/information/notice')">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column width="170" align="right">
              <template #default="{ row }">{{ fmt(row.publishTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :md="10">
        <el-card shadow="never" header="我的待办">
          <el-table :data="summary.recentTodo || []" size="large" :show-header="false">
            <el-table-column>
              <template #default="{ row }">
                <span class="todo-title" @click="$router.push('/flow/todo')">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column width="90" align="right">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.nodeName }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!(summary.recentTodo && summary.recentTodo.length)" description="暂无待办" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px" header="功能导航">
      <div class="quick-nav">
        <div class="nav-item" v-for="n in nav" :key="n.path" @click="$router.push(n.path)">
          <el-icon :color="n.color"><component :is="n.icon" /></el-icon>
          <span>{{ n.label }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardSummary } from '@/api/portal'

const summary = ref({})
const cards = [
  { key: 'todoCount', label: '待办事项', icon: 'List', bg: 'linear-gradient(135deg,#409eff,#5aabff)' },
  { key: 'doneCount', label: '已办事项', icon: 'CircleCheck', bg: 'linear-gradient(135deg,#67c23a,#85d16a)' },
  { key: 'unreadMessage', label: '未读消息', icon: 'Bell', bg: 'linear-gradient(135deg,#e6a23c,#f0bd5e)' },
  { key: 'myDocs', label: '我的公文', icon: 'Document', bg: 'linear-gradient(135deg,#f56c6c,#f59a9a)' }
]
const nav = [
  { label: '发文', icon: 'Edit', color: '#409eff', path: '/document/send' },
  { label: '请假', icon: 'Calendar', color: '#67c23a', path: '/office/leave' },
  { label: '用车', icon: 'Van', color: '#e6a23c', path: '/office/vehicle' },
  { label: '考勤', icon: 'Clock', color: '#f56c6c', path: '/office/attendance' },
  { label: '通讯录', icon: 'UserFilled', color: '#909399', path: '/contacts' },
  { label: '公告', icon: 'ChatDotRound', color: '#9c27b0', path: '/information/notice' },
  { label: '待办', icon: 'Tickets', color: '#00bcd4', path: '/flow/todo' },
  { label: '信息发布', icon: 'Reading', color: '#3f51b5', path: '/information/article' }
]

function fmt(t) {
  return t ? String(t).replace('T', ' ').substring(0, 16) : ''
}

onMounted(async () => {
  const res = await dashboardSummary()
  summary.value = res.data || {}
})
</script>

<style scoped lang="scss">
.stat-card {
  height: 100px;
  border-radius: 8px;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.stat-icon { font-size: 40px; }
.stat-num { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }
.notice-title, .todo-title { cursor: pointer; color: #303133; }
.notice-title:hover, .todo-title:hover { color: #409eff; }
.quick-nav { display: flex; flex-wrap: wrap; gap: 24px; }
.nav-item {
  width: 96px; display: flex; flex-direction: column; align-items: center; gap: 8px;
  cursor: pointer; padding: 12px 0; border-radius: 8px;
  &:hover { background: #f5f7fa; }
  .el-icon { font-size: 28px; }
  span { font-size: 13px; color: #606266; }
}
</style>
