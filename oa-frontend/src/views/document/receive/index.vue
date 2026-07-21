<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="标题"><el-input v-model="query.title" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('document:official:add')">收文登记</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="docNo" label="文号" width="160"><template #default="{ row }">{{ row.docNo || '-' }}</template></el-table-column>
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="fromUnit" label="来文单位" width="160" />
        <el-table-column prop="drafterName" label="登记人" width="100" />
        <el-table-column prop="createTime" label="登记时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <el-button v-if="user.hasPerm('document:official:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" title="收文登记" width="720px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="90px">
        <el-form-item label="来文标题" prop="title"><el-input v-model="form.data.title" /></el-form-item>
        <el-form-item label="来文单位" prop="fromUnit"><el-input v-model="form.data.fromUnit" /></el-form-item>
        <el-form-item label="文种"><el-select v-model="form.data.docCategory"><el-option v-for="d in cats" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" /></el-select></el-form-item>
        <el-form-item label="密级"><el-select v-model="form.data.secrecy"><el-option v-for="d in secrecy" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" /></el-select></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.data.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="form.visible = false">取消</el-button><el-button type="primary" @click="save">登记</el-button></template>
    </el-dialog>

    <el-dialog v-model="view.visible" title="收文详情" width="720px">
      <el-descriptions v-if="view.data" :column="2" border>
        <el-descriptions-item label="标题">{{ view.data.title }}</el-descriptions-item>
        <el-descriptions-item label="来文单位">{{ view.data.fromUnit }}</el-descriptions-item>
        <el-descriptions-item label="文种">{{ dictLabel('oa_doc_category', view.data.docCategory) }}</el-descriptions-item>
        <el-descriptions-item label="登记人">{{ view.data.drafterName }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">正文</el-divider>
      <div style="line-height:1.8" v-html="view.data?.content"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import { pageDoc, getDoc, addDoc, deleteDoc } from '@/api/document'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const cats = useDict('oa_doc_category')
const secrecy = useDict('oa_doc_secrecy')
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, docType: 'receive', title: '' })
const form = reactive({ visible: false, data: { docType: 'receive', secrecy: 0, docCategory: 'report' } })
const view = reactive({ visible: false, data: null })
const rules = { title: [{ required: true, message: '必填', trigger: 'blur' }], fromUnit: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() { loading.value = true; try { const res = await pageDoc(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function openAdd() { form.data = { docType: 'receive', secrecy: 0, docCategory: 'report' }; form.visible = true }
async function save() { await addDoc(form.data); ElMessage.success('登记成功'); form.visible = false; load() }
async function openView(row) { const res = await getDoc(row.id); view.data = res.data; view.visible = true }
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await deleteDoc(row.id); ElMessage.success('已删除'); load() }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
