<template>
  <ApplyPage title="用印申请" :api="api" business-type="seal" perm="office:seal" :columns="columns" :fields="fields" :defaults="defaults" />
</template>

<script setup>
import ApplyPage from '../components/ApplyPage.vue'
import { seal as api } from '@/api/office'

const columns = [
  { prop: 'sealType', label: '印章类型', width: 100, type: 'dict', dict: 'oa_seal_type' },
  { prop: 'fileCount', label: '份数', width: 70 },
  { prop: 'usageDesc', label: '用途', minWidth: 160 }
]
const fields = [
  { prop: 'sealType', label: '印章类型', type: 'select', dict: 'oa_seal_type', required: true },
  { prop: 'fileCount', label: '用印份数', type: 'number', required: true },
  { prop: 'usageDesc', label: '用途说明', type: 'textarea', span: 24 }
]
const defaults = { sealType: 'official', fileCount: 1, status: 0 }
</script>
