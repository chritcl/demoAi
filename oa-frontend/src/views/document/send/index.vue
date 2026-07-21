<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="标题"><el-input v-model="query.title" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="文号"><el-input v-model="query.docNo" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width:120px">
            <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" />
            <el-option label="已发布" :value="2" /><el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('document:official:add')">起草发文</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="docNo" label="文号" width="170" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="docCategory" label="文种" width="100"><template #default="{ row }">{{ dictLabel('oa_doc_category', row.docCategory) }}</template></el-table-column>
        <el-table-column prop="urgency" label="缓急" width="90" align="center"><template #default="{ row }"><el-tag :type="urgType(row.urgency)" size="small">{{ dictLabel('oa_doc_urgency', row.urgency) }}</el-tag></template></el-table-column>
        <el-table-column prop="drafterName" label="拟稿人" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <template v-if="row.status === 0">
              <el-button v-if="user.hasPerm('document:official:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="user.hasPerm('document:official:submit')" link type="success" @click="doSubmit(row)">提交</el-button>
            </template>
            <el-button v-if="user.hasPerm('document:official:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.id ? '编辑发文' : '起草发文'" width="840px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.data.title" /></el-form-item>
        <el-row>
          <el-col :span="8"><el-form-item label="文种"><el-select v-model="form.data.docCategory"><el-option v-for="d in cats" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="缓急"><el-select v-model="form.data.urgency"><el-option v-for="d in urgency" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="密级"><el-select v-model="form.data.secrecy"><el-option v-for="d in secrecy" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="发送范围"><el-input v-model="form.data.recipientScope" /></el-form-item>
        <el-form-item label="正文" prop="content"><el-input v-model="form.data.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="form.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="view.visible" title="公文详情" width="820px">
      <template v-if="view.data">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文号">{{ view.data.docNo || '（未生成）' }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ view.data.title }}</el-descriptions-item>
          <el-descriptions-item label="文种">{{ dictLabel('oa_doc_category', view.data.docCategory) }}</el-descriptions-item>
          <el-descriptions-item label="缓急">{{ dictLabel('oa_doc_urgency', view.data.urgency) }}</el-descriptions-item>
          <el-descriptions-item label="拟稿人">{{ view.data.drafterName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(view.data.status) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">正文</el-divider>
        <div style="line-height:1.8" v-html="view.data.content"></div>
        <el-divider content-position="left">审批轨迹</el-divider>
        <FlowTimeline business-type="document_send" :business-id="view.data.id" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import { pageDoc, getDoc, addDoc, updateDoc, submitDoc, deleteDoc } from '@/api/document'
import Pagination from '@/components/Pagination/index.vue'
import FlowTimeline from '@/components/FlowTimeline.vue'

const user = useUserStore()
const cats = useDict('oa_doc_category')
const urgency = useDict('oa_doc_urgency')
const secrecy = useDict('oa_doc_secrecy')
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, docType: 'send', title: '', docNo: '', status: undefined })
const form = reactive({ visible: false, id: null, data: { docType: 'send', urgency: 0, secrecy: 0, docCategory: 'notice' } })
const view = reactive({ visible: false, data: null })
const rules = { title: [{ required: true, message: '必填', trigger: 'blur' }], content: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try { const res = await pageDoc(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
function reset() { query.title = ''; query.docNo = ''; query.status = undefined; onSearch() }
function openAdd() { form.id = null; form.data = { docType: 'send', urgency: 0, secrecy: 0, docCategory: 'notice' }; form.visible = true }
async function openEdit(row) { const res = await getDoc(row.id); form.id = row.id; form.data = res.data; form.visible = true }
async function save() {
  if (form.id) { form.data.id = form.id; await updateDoc(form.data) } else await addDoc(form.data)
  ElMessage.success('已保存'); form.visible = false; load()
}
async function openView(row) { const res = await getDoc(row.id); view.data = res.data; view.visible = true }
async function doSubmit(row) {
  await ElMessageBox.confirm('确认提交该发文进入审批流程？', '提示', { type: 'warning' })
  await submitDoc(row.id); ElMessage.success('已提交'); load()
}
async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该公文？', '提示', { type: 'warning' })
  await deleteDoc(row.id); ElMessage.success('已删除'); load()
}
function statusText(s) { return ['草稿', '审批中', '已发布', '已驳回'][s] }
function statusType(s) { return ['info', 'warning', 'success', 'danger'][s] }
function urgType(u) { return ['info', 'warning', 'danger'][u] || 'info' }
onMounted(load)
</script>
