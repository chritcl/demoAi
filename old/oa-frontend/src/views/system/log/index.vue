<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="模块"><el-input v-model="query.title" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="操作人"><el-input v-model="query.operName" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button v-if="user.hasPerm('system:log:remove')" type="danger" :icon="'Delete'" @click="clear">清空</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="title" label="模块" width="120" />
        <el-table-column prop="businessType" label="类型" width="80"><template #default="{ row }">{{ ['', '新增', '修改', '删除', '导出'][row.businessType] || '其它' }}</template></el-table-column>
        <el-table-column prop="operName" label="操作人" width="110" />
        <el-table-column prop="requestMethod" label="方法" width="70" />
        <el-table-column prop="operUrl" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column prop="status" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '成功' : '异常' }}</el-tag></template></el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="operTime" label="操作时间" width="170"><template #default="{ row }">{{ fmt(row.operTime) }}</template></el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageLog, clearLog } from '@/api/system'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, title: '', operName: '' })

async function load() { loading.value = true; try { const res = await pageLog(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
async function clear() { await ElMessageBox.confirm('确认清空所有日志？', '提示', { type: 'warning' }); await clearLog(); ElMessage.success('已清空'); load() }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 19) : '' }
onMounted(load)
</script>
