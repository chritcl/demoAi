<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="名称"><el-input v-model="query.assetName" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="query.category" clearable style="width:140px"><el-option v-for="d in cat" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('office:asset:add')">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="assetCode" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="110"><template #default="{ row }">{{ dictLabel('oa_asset_category', row.category) }}</template></el-table-column>
        <el-table-column prop="spec" label="规格" width="140" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column prop="amount" label="价值" width="100" />
        <el-table-column prop="location" label="存放地" width="120" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ dictLabel('oa_asset_status', row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('office:asset:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('office:asset:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑资产' : '新增资产'" width="640px">
      <el-form :model="form.data" label-width="90px">
        <el-row>
          <el-col :span="12"><el-form-item label="资产编号"><el-input v-model="form.data.assetCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资产名称"><el-input v-model="form.data.assetName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分类"><el-select v-model="form.data.category"><el-option v-for="d in cat" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规格"><el-input v-model="form.data.spec" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数量"><el-input-number v-model="form.data.quantity" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="价值"><el-input-number v-model="form.data.amount" :precision="2" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="存放地"><el-input v-model="form.data.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.data.status"><el-option v-for="d in status" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="form.visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import { asset as api } from '@/api/office'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const cat = useDict('oa_asset_category')
const status = useDict('oa_asset_status')
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, assetName: '', category: undefined })
const form = reactive({ visible: false, data: {} })

async function load() { loading.value = true; try { const res = await api.page(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function reset() { query.assetName = ''; query.category = undefined; onSearch() }
function openAdd() { form.data = { quantity: 1, status: 0, category: 'electronic' }; form.visible = true }
async function openEdit(row) { const res = await api.get(row.id); form.data = res.data; form.visible = true }
async function save() { if (form.data.id) await api.update(form.data); else await api.add(form.data); ElMessage.success('保存成功'); form.visible = false; load() }
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await api.remove(row.id); ElMessage.success('已删除'); load() }
function statusType(s) { return ['info', 'success', 'warning', 'danger'][s] }
onMounted(load)
</script>
