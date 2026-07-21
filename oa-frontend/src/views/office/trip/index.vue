<template>
  <ApplyPage title="出差申请" :api="api" business-type="trip" perm="office:trip" :columns="columns" :fields="fields" :defaults="defaults" />
</template>

<script setup>
import ApplyPage from '../components/ApplyPage.vue'
import { trip as api } from '@/api/office'

const columns = [
  { prop: 'destination', label: '目的地', minWidth: 140 },
  { prop: 'startDate', label: '开始', width: 110, type: 'date' },
  { prop: 'endDate', label: '结束', width: 110, type: 'date' },
  { prop: 'days', label: '天数', width: 70 },
  { prop: 'travelMode', label: '交通', width: 90, type: 'dict', dict: 'oa_travel_mode' },
  { prop: 'budget', label: '预算', width: 90 }
]
const fields = [
  { prop: 'destination', label: '目的地', type: 'input', required: true },
  { prop: 'travelMode', label: '交通方式', type: 'select', dict: 'oa_travel_mode' },
  { prop: 'startDate', label: '开始日期', type: 'date', required: true },
  { prop: 'endDate', label: '结束日期', type: 'date', required: true },
  { prop: 'days', label: '天数', type: 'number', precision: 1 },
  { prop: 'budget', label: '预算(元)', type: 'number', precision: 2 },
  { prop: 'reason', label: '事由', type: 'textarea', span: 24 }
]
const defaults = { travelMode: 'plane', status: 0 }
</script>
