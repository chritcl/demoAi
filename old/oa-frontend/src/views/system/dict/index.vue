<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="字典名"><el-input v-model="query.dictName" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="query.dictType" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openType()" v-if="user.hasPerm('system:dict:add')">新增类型</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="dictName" label="字典名称" min-width="160" />
        <el-table-column prop="dictType" label="字典类型" min-width="160" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">{{ row.status === 0 ? '正常' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openData(row)">数据</el-button>
            <el-button v-if="user.hasPerm('system:dict:edit')" link type="primary" @click="openType(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('system:dict:remove')" link type="danger" @click="doDeleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="typeForm.visible" :title="typeForm.data.id ? '编辑字典类型' : '新增字典类型'" width="480px">
      <el-form :model="typeForm.data" label-width="90px">
        <el-form-item label="字典名称"><el-input v-model="typeForm.data.dictName" /></el-form-item>
        <el-form-item label="字典类型"><el-input v-model="typeForm.data.dictType" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="typeForm.data.status"><el-radio :value="0">正常</el-radio><el-radio :value="1">停用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="typeForm.visible = false">取消</el-button><el-button type="primary" @click="saveType">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="dataDrawer.visible" :title="'字典数据 - ' + dataDrawer.type" size="540px">
      <div style="text-align:right;margin-bottom:8px"><el-button type="primary" :icon="'Plus'" size="small" @click="openDataItem()">新增</el-button></div>
      <el-table :data="dataList" border size="small">
        <el-table-column prop="dictLabel" label="标签" />
        <el-table-column prop="dictValue" label="值" width="100" />
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="primary" @click="openDataItem(row)">编辑</el-button><el-button link type="danger" @click="doDeleteData(row)">删</el-button></template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="dataForm.visible" :title="dataForm.data.id ? '编辑数据' : '新增数据'" width="440px" append-to-body>
      <el-form :model="dataForm.data" label-width="80px">
        <el-form-item label="标签"><el-input v-model="dataForm.data.dictLabel" /></el-form-item>
        <el-form-item label="值"><el-input v-model="dataForm.data.dictValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.data.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dataForm.visible = false">取消</el-button><el-button type="primary" @click="saveDataItem">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageDictType, addDictType, updateDictType, deleteDictType, dictData, addDictData, updateDictData, deleteDictData } from '@/api/system'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, dictName: '', dictType: '' })
const typeForm = reactive({ visible: false, data: {} })
const dataDrawer = reactive({ visible: false, type: '' })
const dataList = ref([])
const dataForm = reactive({ visible: false, data: {} })

async function load() { loading.value = true; try { const res = await pageDictType(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function openType(row) { typeForm.data = row ? { ...row } : { status: 0 }; typeForm.visible = true }
async function saveType() { if (typeForm.data.id) await updateDictType(typeForm.data); else await addDictType(typeForm.data); ElMessage.success('保存成功'); typeForm.visible = false; load() }
async function doDeleteType(row) { await ElMessageBox.confirm('删除字典类型将同时删除其数据，确认？', '提示', { type: 'warning' }); await deleteDictType(row.id); ElMessage.success('已删除'); load() }

async function openData(row) { dataDrawer.type = row.dictType; dataDrawer.visible = true; await loadData(row.dictType) }
async function loadData(type) { const res = await dictData(type); dataList.value = res.data || [] }
function openDataItem(item) { dataForm.data = item ? { ...item } : { dictType: dataDrawer.type, sort: 0, status: 0 }; dataForm.visible = true }
async function saveDataItem() { if (dataForm.data.id) await updateDictData(dataForm.data); else await addDictData(dataForm.data); ElMessage.success('保存成功'); dataForm.visible = false; loadData(dataDrawer.type) }
async function doDeleteData(item) { await deleteDictData(item.id); ElMessage.success('已删除'); loadData(dataDrawer.type) }
onMounted(load)
</script>
