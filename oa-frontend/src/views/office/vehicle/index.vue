<template>
  <ApplyPage title="用车申请" :api="api" business-type="vehicle" perm="office:vehicle" :columns="columns" :fields="fields" :defaults="defaults" />
</template>

<script setup>
import ApplyPage from '../components/ApplyPage.vue'
import { vehicle as api } from '@/api/office'

const columns = [
  { prop: 'useTime', label: '用车时间', width: 150, type: 'datetime' },
  { prop: 'destination', label: '目的地', minWidth: 140 },
  { prop: 'carType', label: '车型', width: 90, type: 'dict', dict: 'oa_vehicle_type' },
  { prop: 'passengers', label: '人数', width: 70 }
]
const fields = [
  { prop: 'useTime', label: '用车时间', type: 'datetime', required: true },
  { prop: 'returnTime', label: '返回时间', type: 'datetime' },
  { prop: 'originPlace', label: '出发地', type: 'input' },
  { prop: 'destination', label: '目的地', type: 'input', required: true },
  { prop: 'carType', label: '要求车型', type: 'select', dict: 'oa_vehicle_type' },
  { prop: 'passengers', label: '随行人数', type: 'number' },
  { prop: 'reason', label: '事由', type: 'textarea', span: 24 }
]
const defaults = { carType: 'car', status: 0 }
</script>
