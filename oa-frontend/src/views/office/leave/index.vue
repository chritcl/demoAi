<template>
  <ApplyPage title="请假申请" :api="api" business-type="leave" perm="office:leave" :columns="columns" :fields="fields" :defaults="defaults" />
</template>

<script setup>
import ApplyPage from '../components/ApplyPage.vue'
import { leave as api } from '@/api/office'

const columns = [
  { prop: 'leaveType', label: '假别', width: 90, type: 'dict', dict: 'oa_leave_type' },
  { prop: 'startDate', label: '开始', width: 110, type: 'date' },
  { prop: 'endDate', label: '结束', width: 110, type: 'date' },
  { prop: 'days', label: '天数', width: 80 },
  { prop: 'reason', label: '事由', minWidth: 160 }
]
const fields = [
  { prop: 'leaveType', label: '假别', type: 'select', dict: 'oa_leave_type', required: true },
  { prop: 'days', label: '天数', type: 'number', precision: 1, required: true },
  { prop: 'startDate', label: '开始日期', type: 'date', required: true },
  { prop: 'endDate', label: '结束日期', type: 'date', required: true },
  { prop: 'reason', label: '事由', type: 'textarea', span: 24 }
]
const defaults = { leaveType: 'annual', status: 0 }
</script>
