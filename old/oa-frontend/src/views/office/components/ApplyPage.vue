<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:120px" @change="onSearch">
            <el-option label="草稿" :value="0" /><el-option label="审批中" :value="1" />
            <el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="'Refresh'" @click="reset">刷新</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm(addPerm)">新增{{ title }}</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label"
          :width="c.width" :min-width="c.minWidth" :align="c.align || 'left'">
          <template #default="{ row }">
            <span v-if="c.type === 'dict'">{{ dictLabel(c.dict, row[c.prop]) }}</span>
            <el-tag v-else-if="c.type === 'tag' && row[c.prop] !== undefined && row[c.prop] !== null" size="small">{{ row[c.prop] }}</el-tag>
            <span v-else>{{ fmt(row[c.prop], c.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime, 'datetime') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <template v-if="row.status === 0">
              <el-button v-if="user.hasPerm(editPerm)" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="user.hasPerm(submitPerm)" link type="success" @click="doSubmit(row)">提交</el-button>
            </template>
            <el-button v-if="user.hasPerm(removePerm)" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.id ? ('编辑' + title) : ('新增' + title)" width="680px">
      <el-form ref="formRef" :model="form.data" label-width="100px">
        <el-row v-for="grp in fieldRows" :key="grp[0].prop">
          <el-col :span="f.span || 12" v-for="f in grp" :key="f.prop">
            <el-form-item :label="f.label" :prop="f.prop" :rules="f.required ? [{ required: true, message: '必填', trigger: 'change' }] : undefined">
              <el-select v-if="f.type === 'select'" v-model="form.data[f.prop]" placeholder="请选择">
                <el-option v-for="d in dictCache(f.dict)" :key="d.dictValue" :label="d.dictLabel" :value="f.number ? Number(d.dictValue) : d.dictValue" />
              </el-select>
              <el-date-picker v-else-if="f.type === 'date'" v-model="form.data[f.prop]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              <el-date-picker v-else-if="f.type === 'datetime'" v-model="form.data[f.prop]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
              <el-input-number v-else-if="f.type === 'number'" v-model="form.data[f.prop]" :precision="f.precision" :min="0" style="width:100%" />
              <el-input v-else-if="f.type === 'textarea'" v-model="form.data[f.prop]" type="textarea" :rows="2" />
              <el-input v-else v-model="form.data[f.prop]" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="form.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="view.visible" :title="title + '详情'" width="720px">
      <el-descriptions v-if="view.data" :column="2" border>
        <el-descriptions-item label="申请人">{{ view.data.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(view.data.status)" size="small">{{ statusText(view.data.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item v-for="c in columns" :key="c.prop" :label="c.label">
          <span v-if="c.type === 'dict'">{{ dictLabel(c.dict, view.data[c.prop]) }}</span>
          <span v-else>{{ fmt(view.data[c.prop], c.type) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">审批轨迹</el-divider>
      <FlowTimeline :business-type="businessType" :business-id="view.data ? view.data.id : ''" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useDict, dictLabel } from '@/composables/useDict'
import Pagination from '@/components/Pagination/index.vue'
import FlowTimeline from '@/components/FlowTimeline.vue'

const props = defineProps({
  title: String,
  api: Object,
  businessType: String,
  perm: String, // e.g. office:leave
  columns: { type: Array, default: () => [] },
  fields: { type: Array, default: () => [] }, // formFields
  defaults: { type: Object, default: () => ({}) }
})

const user = useUserStore()
const addPerm = computed(() => props.perm + ':add')
const editPerm = computed(() => props.perm + ':edit')
const submitPerm = computed(() => props.perm + ':submit')
const removePerm = computed(() => props.perm + ':remove')

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, mineOnly: true, status: undefined })
const formRef = ref()
const form = reactive({ visible: false, id: null, data: { ...props.defaults } })
const view = reactive({ visible: false, data: null })

const fieldRows = computed(() => {
  const rows = []
  let i = 0
  while (i < props.fields.length) {
    const cur = props.fields[i]
    const span = cur.span || 12
    if (span === 24) { rows.push([cur]); i++ }
    else if (i + 1 < props.fields.length && (props.fields[i + 1].span || 12) === 12) { rows.push([cur, props.fields[i + 1]]); i += 2 }
    else { rows.push([cur]); i++ }
  }
  return rows
})

// 预取本页用到的字典，保证响应式
const dictMap = {}
;[...props.columns, ...props.fields].forEach((f) => {
  if (f.dict && !dictMap[f.dict]) dictMap[f.dict] = useDict(f.dict)
})
function dictCache(type) { return type && dictMap[type] ? dictMap[type].value : [] }

async function load() {
  loading.value = true
  try { const res = await props.api.page(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
function reset() { query.status = undefined; onSearch() }
function openAdd() { form.id = null; form.data = { ...props.defaults }; form.visible = true }
async function openEdit(row) { const res = await props.api.get(row.id); form.id = row.id; form.data = res.data; form.visible = true }
async function save() {
  if (form.id) { form.data.id = form.id; await props.api.update(form.data) } else await props.api.add(form.data)
  ElMessage.success('保存成功'); form.visible = false; load()
}
async function openView(row) { const res = await props.api.get(row.id); view.data = res.data; view.visible = true }
async function doSubmit(row) {
  await ElMessageBox.confirm(`确认提交该${props.title}进入审批？`, '提示', { type: 'warning' })
  await props.api.submit(row.id); ElMessage.success('已提交'); load()
}
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await props.api.remove(row.id); ElMessage.success('已删除'); load() }

function statusText(s) { return ['草稿', '审批中', '已通过', '已驳回'][s] }
function statusType(s) { return ['info', 'warning', 'success', 'danger'][s] }
function fmt(v, type) {
  if (v === null || v === undefined || v === '') return '-'
  if (type === 'datetime') return String(v).replace('T', ' ').substring(0, 16)
  if (type === 'date') return String(v).substring(0, 10)
  return v
}
onMounted(load)
</script>
