<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="审批办理" left-arrow @click-left="$router.back()" fixed />
    <van-skeleton title :row="3" :loading="loading">
      <van-cell-group inset style="margin-top:12px">
        <van-cell title="标题" :value="task.title" />
        <van-cell title="类型" :value="bizLabel(task.businessType)" />
        <van-cell title="环节" :value="task.nodeName" />
        <van-cell title="发起人" :value="task.startUserName" />
      </van-cell-group>

      <div class="sec-title">审批轨迹</div>
      <van-steps direction="vertical" :active="steps.length - 1">
        <van-step v-for="s in steps" :key="s.id">
          <h4>{{ s.nodeName }} · {{ statusText(s.status) }}</h4>
          <p>{{ s.assigneeName }} <template v-if="s.actionUserName">→ {{ s.actionUserName }}</template></p>
          <p class="muted">{{ fmt(s.actionTime || s.createTime) }}</p>
          <p v-if="s.comment">{{ s.comment }}</p>
        </van-step>
      </van-steps>

      <div class="sec-title">审批意见</div>
      <van-field v-model="comment" type="textarea" placeholder="请输入审批意见" rows="3" autosize style="background:#fff" />

      <div style="display:flex;gap:12px;padding:16px">
        <van-button type="danger" block @click="doReject">驳回</van-button>
        <van-button type="success" block @click="doApprove">通过</van-button>
      </div>
    </van-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { todoTasks, tasksOfBusiness, approveTask, rejectTask } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const task = ref({})
const steps = ref([])
const comment = ref('')

async function load() {
  loading.value = true
  try {
    const todo = (await todoTasks()).data || []
    task.value = todo.find((t) => String(t.id) === String(route.params.id)) || {}
    if (task.value.businessId) {
      steps.value = (await tasksOfBusiness(task.value.businessType, task.value.businessId)).data || []
    }
  } finally { loading.value = false }
}
async function doApprove() {
  await approveTask(route.params.id, comment.value)
  showSuccessToast('已通过'); router.back()
}
async function doReject() {
  await rejectTask(route.params.id, comment.value)
  showSuccessToast('已驳回'); router.back()
}
function bizLabel(t) { return { leave: '请假', vehicle: '用车', seal: '用印', trip: '出差', document_send: '发文' }[t] || t }
function statusText(s) { return { pending: '待办理', done: '已通过', rejected: '已驳回', transferred: '已转办' }[s] || s }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>

<style scoped>
.sec-title { padding: 16px 16px 8px; color: #969799; font-size: 13px; }
</style>
