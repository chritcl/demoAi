<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="名称"><el-input v-model="query.name" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('office:supply:add')">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="110"><template #default="{ row }">{{ dictLabel('oa_supply_category', row.category) }}</template></el-table-column>
        <el-table-column prop="spec" label="规格" width="140" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="stock" label="库存" width="90" align="center">
          <template #default="{ row }"><el-tag :type="row.stock <= (row.warningStock || 0) ? 'danger' : 'success'" size="small">{{ row.stock }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警" width="70" align="center" />
        <el-table-column prop="price" label="单价" width="90" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('office:supply:edit')" link type="success" @click="adjust(row, 1)">入库</el-button>
            <el-button v-if="user.hasPerm('office:supply:edit')" link type="warning" @click="adjust(row, -1)">出库</el-button>
            <el-button v-if="user.hasPerm('office:supply:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('office:supply:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑办公用品' : '新增办公用品'" width="560px">
      <el-form :model="form.data" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.data.name" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.data.category"><el-option v-for="d in cat" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" /></el-select></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.data.spec" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.data.unit" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.data.stock" :min="0" /></el-form-item>
        <el-form-item label="预警库存"><el-input-number v-model="form.data.warningStock" :min="0" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="form.data.price" :precision="2" :min="0" /></el-form-item>
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
import { supply as api } from '@/api/office'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const cat = useDict('oa_supply_category')
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, name: '' })
const form = reactive({ visible: false, data: {} })

async function load() { loading.value = true; try { const res = await api.page(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function openAdd() { form.data = { stock: 0, warningStock: 0, category: 'stationery' }; form.visible = true }
async function openEdit(row) { const res = await api.get(row.id); form.data = res.data; form.visible = true }
async function save() { if (form.data.id) await api.update(form.data); else await api.add(form.data); ElMessage.success('保存成功'); form.visible = false; load() }
async function adjust(row, dir) {
  const { value } = await ElMessageBox.prompt(`请输入${dir > 0 ? '入库' : '出库'}数量`, dir > 0 ? '入库' : '出库', {
    inputType: 'number', inputValidator: (v) => v > 0 || '请输入正数'
  })
  await api.adjust(row.id, Number(value) * dir); ElMessage.success('操作成功'); load()
}
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await api.remove(row.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
