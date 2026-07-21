<template>
  <div class="flow-timeline">
    <el-timeline v-if="tasks.length">
      <el-timeline-item
        v-for="t in tasks"
        :key="t.id"
        :timestamp="t.actionTime || t.createTime"
        placement="top"
        :type="stampType(t.status)"
      >
        <div class="node">
          <span class="name">{{ t.nodeName }}</span>
          <el-tag size="small" :type="statusTag(t.status)" effect="plain">{{ statusText(t.status) }}</el-tag>
        </div>
        <div class="line">办理人：{{ t.assigneeName || '-' }} <template v-if="t.actionUserName"> → {{ t.actionUserName }}</template></div>
        <div class="line" v-if="t.comment">意见：{{ t.comment }}</div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无审批记录" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { tasksOfBusiness } from '@/api/flow'

const props = defineProps({
  businessType: { type: String, default: '' },
  businessId: { type: [Number, String], default: '' }
})

const tasks = ref([])

function load() {
  if (!props.businessType || !props.businessId) {
    tasks.value = []
    return
  }
  tasksOfBusiness(props.businessType, props.businessId).then((res) => {
    tasks.value = res.data || []
  })
}

function statusText(s) {
  return { pending: '待办理', done: '已通过', rejected: '已驳回', transferred: '已转办' }[s] || s
}
function statusTag(s) {
  return { pending: 'warning', done: 'success', rejected: 'danger', transferred: 'info' }[s] || 'info'
}
function stampType(s) {
  return { pending: 'primary', done: 'success', rejected: 'danger', transferred: 'info' }[s] || 'primary'
}

watch(() => [props.businessType, props.businessId], load, { immediate: true })
</script>

<style scoped>
.node { display: flex; gap: 8px; align-items: center; font-weight: 600; }
.line { color: #909399; font-size: 13px; margin-top: 2px; }
</style>
