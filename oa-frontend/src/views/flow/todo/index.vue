<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-tabs v-model="active" @tab-change="load">
        <el-tab-pane label="待办" name="todo">
          <el-table :data="todo" v-loading="loading" border stripe>
            <el-table-column prop="title" label="标题" min-width="240" />
            <el-table-column prop="businessType" label="类型" width="110"><template #default="{ row }">{{ bizLabel(row.businessType) }}</template></el-table-column>
            <el-table-column prop="nodeName" label="环节" width="140" />
            <el-table-column prop="startUserName" label="发起人" width="110" />
            <el-table-column prop="createTime" label="到达时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }"><el-button type="primary" link @click="open(row)">处理</el-button></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已办" name="done">
          <el-table :data="done" v-loading="loading" border stripe>
            <el-table-column prop="title" label="标题" min-width="240" />
            <el-table-column prop="nodeName" label="环节" width="140" />
            <el-table-column prop="status" label="结果" width="100"><template #default="{ row }"><el-tag :type="rt(row.status)" size="small">{{ rtText(row.status) }}</el-tag></template></el-table-column>
            <el-table-column prop="actionTime" label="办理时间" width="170"><template #default="{ row }">{{ fmt(row.actionTime) }}</template></el-table-column>
            <el-table-column prop="comment" label="意见" min-width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dlg.visible" title="审批处理" width="680px">
      <FlowTimeline v-if="dlg.row" :business-type="dlg.row.businessType" :business-id="dlg.row.businessId" />
      <el-divider>转办(可选)</el-divider>
      <el-row :gutter="12">
        <el-col :span="14"><el-select v-model="dlg.toUserId" placeholder="选择转办人" filterable><el-option v-for="u in users" :key="u.id" :label="u.nickname" :value="u.id" /></el-select></el-col>
        <el-col :span="10"><el-button @click="doTransfer">转办</el-button></el-col>
      </el-row>
      <el-divider>审批意见</el-divider>
      <el-input v-model="dlg.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
      <template #footer>
        <el-button @click="dlg.visible = false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="primary" @click="doApprove">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { todoTasks, doneTasks, approveTask, rejectTask, transferTask } from '@/api/flow'
import { pageUser } from '@/api/system'
import FlowTimeline from '@/components/FlowTimeline.vue'

const active = ref('todo'); const loading = ref(false)
const todo = ref([]); const done = ref([]); const users = ref([])
const dlg = reactive({ visible: false, row: null, comment: '', toUserId: null })

async function load() {
  loading.value = true
  try { if (active.value === 'todo') todo.value = (await todoTasks()).data || []; else done.value = (await doneTasks()).data || [] }
  finally { loading.value = false }
}
async function loadUsers() { const res = await pageUser({ pageNum: 1, pageSize: 200 }); users.value = res.data.list }
function open(row) { dlg.row = row; dlg.comment = ''; dlg.toUserId = null; dlg.visible = true }
async function doApprove() { await approveTask(dlg.row.id, dlg.comment); ElMessage.success('已通过'); dlg.visible = false; load() }
async function doReject() { await rejectTask(dlg.row.id, dlg.comment); ElMessage.success('已驳回'); dlg.visible = false; load() }
async function doTransfer() {
  if (!dlg.toUserId) return ElMessage.warning('请选择转办人')
  await transferTask(dlg.row.id, dlg.toUserId, dlg.comment); ElMessage.success('已转办'); dlg.visible = false; load()
}
function bizLabel(t) { return { leave: '请假', vehicle: '用车', seal: '用印', trip: '出差', document_send: '发文' }[t] || t }
function rt(s) { return { done: 'success', rejected: 'danger', transferred: 'info' }[s] || 'info' }
function rtText(s) { return { done: '通过', rejected: '驳回', transferred: '转办' }[s] || s }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(() => { load(); loadUsers() })
</script>
