<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>站内消息</span>
          <el-button type="primary" size="small" @click="readAll" :disabled="!list.some(i => i.isRead === 0)">全部已读</el-button>
        </div>
      </template>
      <el-radio-group v-model="query.isRead" @change="onSearch" style="margin-bottom:12px">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">未读</el-radio-button>
        <el-radio-button :value="1">已读</el-radio-button>
      </el-radio-group>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="标题" min-width="220">
          <template #default="{ row }">
            <el-badge is-dot :hidden="row.isRead === 1">
              <span :class="{ unread: row.isRead === 0 }">{{ row.title }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="sendUserName" label="发送人" width="120" />
        <el-table-column prop="createTime" label="时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="read(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="detail.visible" title="消息详情" width="520px">
      <div v-if="detail.data">
        <h3 style="margin-top:0">{{ detail.data.title }}</h3>
        <div style="color:#909399;font-size:13px;margin-bottom:12px">来自：{{ detail.data.sendUserName }} ｜ {{ fmt(detail.data.createTime) }}</div>
        <div style="line-height:1.8">{{ detail.data.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageMessage, readMessage, readAllMessage, unreadCount } from '@/api/portal'
import Pagination from '@/components/Pagination/index.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, isRead: undefined })
const detail = reactive({ visible: false, data: null })

async function load() {
  loading.value = true
  try { const res = await pageMessage(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
async function read(row) {
  if (row.isRead === 0) { await readMessage(row.id); row.isRead = 1 }
  detail.data = row; detail.visible = true
}
async function readAll() {
  await readAllMessage(); ElMessage.success('已全部标记已读'); load()
}
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.unread { font-weight: 600; color: #303133; }
</style>
