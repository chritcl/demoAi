<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="标题"><el-input v-model="query.title" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="栏目">
          <el-select v-model="query.category" clearable style="width: 140px">
            <el-option v-for="d in cats" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 120px">
            <el-option label="草稿" :value="0" /><el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" /><el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('portal:article:add')">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="category" label="栏目" width="110">
          <template #default="{ row }">{{ dictLabel('oa_article_category', row.category) }}</template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="110" />
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170"><template #default="{ row }">{{ fmt(row.publishTime) }}</template></el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <template v-if="row.status === 0 || row.status === 3">
              <el-button v-if="user.hasPerm('portal:article:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="user.hasPerm('portal:article:edit')" link type="success" @click="doSubmit(row)">提交审核</el-button>
            </template>
            <template v-if="row.status === 1 && user.hasPerm('portal:article:audit')">
              <el-button link type="success" @click="doAudit(row, true)">通过</el-button>
              <el-button link type="danger" @click="doAudit(row, false)">驳回</el-button>
            </template>
            <el-button v-if="user.hasPerm('portal:article:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.id ? '编辑文章' : '新增文章'" width="820px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.data.title" /></el-form-item>
        <el-form-item label="栏目">
          <el-select v-model="form.data.category">
            <el-option v-for="d in cats" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.data.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="正文" prop="content"><el-input v-model="form.data.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="form.visible = false">取消</el-button>
        <el-button @click="save">存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="view.visible" title="文章详情" width="720px">
      <div v-if="view.data">
        <h2 style="text-align:center;margin:0 0 8px">{{ view.data.title }}</h2>
        <div style="text-align:center;color:#909399;font-size:13px">作者：{{ view.data.author }} ｜ 浏览：{{ view.data.viewCount }}</div>
        <el-divider />
        <div style="line-height:1.8" v-html="view.data.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import { pageArticle, getArticle, addArticle, updateArticle, submitArticle, passArticle, rejectArticle, deleteArticle } from '@/api/portal'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const cats = useDict('oa_article_category')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, title: '', category: undefined, status: undefined })
const form = reactive({ visible: false, id: null, data: { category: 'dynamic' } })
const view = reactive({ visible: false, data: null })
const rules = { title: [{ required: true, message: '必填', trigger: 'blur' }], content: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try { const res = await pageArticle(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
function reset() { query.title = ''; query.category = undefined; query.status = undefined; onSearch() }
function openAdd() { form.id = null; form.data = { category: 'dynamic', status: 0 }; form.visible = true }
async function openEdit(row) { const res = await getArticle(row.id); form.id = row.id; form.data = res.data; form.visible = true }
async function save() {
  form.data.status = 0
  if (form.id) { form.data.id = form.id; await updateArticle(form.data) } else await addArticle(form.data)
  ElMessage.success('已保存'); form.visible = false; load()
}
async function openView(row) { const res = await getArticle(row.id); view.data = res.data; view.visible = true }
async function doSubmit(row) { await submitArticle(row.id); ElMessage.success('已提交审核'); load() }
async function doAudit(row, pass) {
  if (pass) { await passArticle(row.id, '审核通过') ; ElMessage.success('已发布') }
  else {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', { inputType: 'textarea' })
    await rejectArticle(row.id, value); ElMessage.success('已驳回')
  }
  load()
}
async function doDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await deleteArticle(row.id); ElMessage.success('已删除'); load()
}
function statusText(s) { return ['草稿', '待审核', '已发布', '已驳回'][s] }
function statusType(s) { return ['info', 'warning', 'success', 'danger'][s] }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
