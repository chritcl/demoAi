<template>
  <div class="app-container">
    <el-tabs v-model="active" @tab-change="load">
      <el-tab-pane label="我的待办" name="todo">
        <el-table :data="todo" v-loading="loading" border stripe>
          <el-table-column prop="title" label="标题" min-width="240" />
          <el-table-column prop="businessType" label="类型" width="120">
            <template #default="{ row }">{{ bizLabel(row.businessType) }}</template>
          </el-table-column>
          <el-table-column prop="nodeName" label="当前环节" width="140" />
          <el-table-column prop="startUserName" label="发起人" width="120" />
          <el-table-column prop="createTime" label="到达时间" width="170">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handle(row)">去处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的已办" name="done">
        <el-table :data="done" v-loading="loading" border stripe>
          <el-table-column prop="title" label="标题" min-width="240" />
          <el-table-column prop="nodeName" label="环节" width="140" />
          <el-table-column prop="status" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="resultType(row.status)" size="small">{{ resultText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="actionTime" label="办理时间" width="170">
            <template #default="{ row }">{{ fmt(row.actionTime) }}</template>
          </el-table-column>
          <el-table-column prop="comment" label="意见" min-width="160" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的发起" name="mine">
        <el-table :data="mine" v-loading="loading" border stripe>
          <el-table-column prop="title" label="标题" min-width="240" />
          <el-table-column prop="businessType" label="类型" width="120">
            <template #default="{ row }">{{ bizLabel(row.businessType) }}</template>
          </el-table-column>
          <el-table-column prop="currentNodeName" label="当前环节" width="140" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="instType(row.status)" size="small">{{ instText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发起时间" width="170">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialog.visible" title="流程审批" width="640px">
      <FlowTimeline :business-type="dialog.businessType" :business-id="dialog.businessId" />
      <el-divider>审批意见</el-divider>
      <el-input v-model="dialog.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="primary" @click="doApprove">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { todoTasks, doneTasks, mineInstances, approveTask, rejectTask } from '@/api/flow'
import FlowTimeline from '@/components/FlowTimeline.vue'

const active = ref('todo')
const loading = ref(false)
const todo = ref([])
const done = ref([])
const mine = ref([])
const dialog = reactive({ visible: false, taskId: null, comment: '', businessType: '', businessId: null })

async function load() {
  loading.value = true
  try {
    if (active.value === 'todo') todo.value = (await todoTasks()).data || []
    else if (active.value === 'done') done.value = (await doneTasks()).data || []
    else mine.value = (await mineInstances()).data || []
  } finally {
    loading.value = false
  }
}

function handle(row) {
  dialog.taskId = row.id
  dialog.businessType = row.businessType
  dialog.businessId = row.businessId
  dialog.comment = ''
  dialog.visible = true
}
async function doApprove() {
  await approveTask(dialog.taskId, dialog.comment)
  ElMessage.success('审批通过')
  dialog.visible = false
  load()
}
async function doReject() {
  await rejectTask(dialog.taskId, dialog.comment)
  ElMessage.success('已驳回')
  dialog.visible = false
  load()
}

function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
function bizLabel(t) {
  return { leave: '请假', vehicle: '用车', seal: '用印', trip: '出差', document_send: '发文' }[t] || t
}
function resultText(s) { return { done: '通过', rejected: '驳回', transferred: '转办' }[s] || s }
function resultType(s) { return { done: 'success', rejected: 'danger', transferred: 'info' }[s] || 'info' }
function instText(s) { return { running: '进行中', done: '已完成', terminated: '已终止' }[s] || s }
function instType(s) { return { running: 'warning', done: 'success', terminated: 'danger' }[s] || 'info' }

onMounted(load)
</script>
