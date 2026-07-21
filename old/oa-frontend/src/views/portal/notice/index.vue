<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="标题" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" :value="0" /><el-option label="已发布" :value="1" /><el-option label="已撤回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('portal:notice:add')">新增公告</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">{{ dictLabel('oa_notice_type', row.category) }}</template>
        </el-table-column>
        <el-table-column prop="top" label="置顶" width="80" align="center">
          <template #default="{ row }"><el-tag v-if="row.top === 1" type="danger" size="small">置顶</el-tag></template>
        </el-table-column>
        <el-table-column prop="readCount" label="阅读" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishUserName" label="发布人" width="110" />
        <el-table-column prop="publishTime" label="发布时间" width="170">
          <template #default="{ row }">{{ fmt(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <el-button v-if="row.status !== 1 && user.hasPerm('portal:notice:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 1 && user.hasPerm('portal:notice:publish')" link type="success" @click="doPublish(row)">发布</el-button>
            <el-button v-if="row.status === 1 && user.hasPerm('portal:notice:publish')" link type="warning" @click="doWithdraw(row)">撤回</el-button>
            <el-button v-if="user.hasPerm('portal:notice:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.id ? '编辑公告' : '新增公告'" width="820px" @close="form.id = null">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.data.title" /></el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.data.category">
                <el-option v-for="d in noticeType" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="置顶"><el-switch v-model="form.data.top" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="摘要"><el-input v-model="form.data.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="正文" prop="content"><el-input v-model="form.data.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="form.visible = false">取消</el-button>
        <el-button @click="save(0)">存草稿</el-button>
        <el-button type="primary" @click="save(1)">保存并发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="view.visible" title="公告详情" width="720px">
      <div v-if="view.data" class="view-box">
        <h2>{{ view.data.title }}</h2>
        <div class="meta">发布人：{{ view.data.publishUserName }} ｜ 发布时间：{{ fmt(view.data.publishTime) }} ｜ 阅读：{{ view.data.readCount }}</div>
        <el-divider />
        <div class="content" v-html="view.data.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import { pageNotice, addNotice, updateNotice, publishNotice, withdrawNotice, deleteNotice, getNotice } from '@/api/portal'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const noticeType = useDict('oa_notice_type')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, title: '', status: undefined })

const formRef = ref()
const form = reactive({ visible: false, id: null, data: {} })
const view = reactive({ visible: false, data: null })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await pageNotice(query)
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
function reset() { query.title = ''; query.status = undefined; onSearch() }

function openAdd() { form.id = null; form.data = { top: 0, category: '1' }; form.visible = true }
async function openEdit(row) {
  const res = await getNotice(row.id)
  form.id = row.id; form.data = res.data; form.visible = true
}
async function save(status) {
  form.data.status = status
  if (form.id) { form.data.id = form.id; await updateNotice(form.data) }
  else await addNotice(form.data)
  ElMessage.success('保存成功')
  form.visible = false
  load()
}
async function openView(row) { const res = await getNotice(row.id); view.data = res.data; view.visible = true }
async function doPublish(row) { await publishNotice(row.id); ElMessage.success('已发布'); load() }
async function doWithdraw(row) { await withdrawNotice(row.id); ElMessage.success('已撤回'); load() }
async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该公告？', '提示', { type: 'warning' })
  await deleteNotice(row.id); ElMessage.success('已删除'); load()
}

function statusText(s) { return ['草稿', '已发布', '已撤回'][s] }
function statusType(s) { return ['info', 'success', 'warning'][s] }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }

onMounted(load)
</script>

<style scoped>
.view-box h2 { margin: 0 0 8px; text-align: center; }
.view-box .meta { text-align: center; color: #909399; font-size: 13px; }
.view-box .content { line-height: 1.8; }
</style>
