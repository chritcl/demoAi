<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header" style="font-weight:600">公文查询</div>
      <el-form inline @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="query.docType" clearable placeholder="全部" style="width:120px">
            <el-option label="发文" value="send" /><el-option label="收文" value="receive" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="query.title" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="文号"><el-input v-model="query.docNo" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item><el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="docType" label="类型" width="80"><template #default="{ row }">{{ row.docType === 'send' ? '发文' : '收文' }}</template></el-table-column>
        <el-table-column prop="docNo" label="文号" width="170" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="docCategory" label="文种" width="100"><template #default="{ row }">{{ dictLabel('oa_doc_category', row.docCategory) }}</template></el-table-column>
        <el-table-column prop="drafterName" label="拟稿/登记" width="110" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType(row.status, row.docType)" size="small">{{ statusText(row.status, row.docType) }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { dictLabel } from '@/composables/useDict'
import { pageDoc } from '@/api/document'
import Pagination from '@/components/Pagination/index.vue'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, docType: undefined, title: '', docNo: '' })
async function load() { loading.value = true; try { const res = await pageDoc(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function statusText(s, t) { if (t === 'receive') return '已登记'; return ['草稿', '审批中', '已发布', '已驳回'][s] }
function statusType(s, t) { if (t === 'receive') return 'success'; return ['info', 'warning', 'success', 'danger'][s] }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
