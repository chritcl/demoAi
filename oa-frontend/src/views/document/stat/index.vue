<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :sm="12" :md="8" v-for="c in cards" :key="c.key">
        <div class="stat-card" :style="{ background: c.bg }">
          <div class="num">{{ stats[c.key] ?? 0 }}</div>
          <div class="label">{{ c.label }}</div>
        </div>
      </el-col>
    </el-row>
    <el-card shadow="never" style="margin-top:16px" header="发文状态分布">
      <div class="bars">
        <div class="bar-item" v-for="b in bars" :key="b.key">
          <div class="bar-label">{{ b.label }}</div>
          <div class="bar-track"><div class="bar-fill" :style="{ width: pct(b.key) + '%', background: b.color }"></div></div>
          <div class="bar-num">{{ stats[b.key] ?? 0 }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { docStatistics } from '@/api/document'

const stats = ref({})
const cards = [
  { key: 'sendTotal', label: '发文总数', bg: 'linear-gradient(135deg,#409eff,#5aabff)' },
  { key: 'receiveTotal', label: '收文总数', bg: 'linear-gradient(135deg,#67c23a,#85d16a)' },
  { key: 'published', label: '已发布', bg: 'linear-gradient(135deg,#909399,#b1b3b8)' }
]
const bars = [
  { key: 'draft', label: '草稿', color: '#909399' },
  { key: 'processing', label: '审批中', color: '#e6a23c' },
  { key: 'published', label: '已发布', color: '#67c23a' },
  { key: 'rejected', label: '已驳回', color: '#f56c6c' }
]
function pct(k) {
  const total = (stats.value.sendTotal || 0) * 1 || 1
  return Math.min(100, Math.round(((stats.value[k] || 0) / total) * 100))
}
onMounted(async () => { const res = await docStatistics(); stats.value = res.data || {} })
</script>

<style scoped lang="scss">
.stat-card { height: 100px; border-radius: 8px; color: #fff; display: flex; flex-direction: column; justify-content: center; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.num { font-size: 32px; font-weight: 700; }
.label { font-size: 13px; opacity: .9; margin-top: 4px; }
.bars { display: flex; flex-direction: column; gap: 16px; padding: 8px 0; }
.bar-item { display: flex; align-items: center; gap: 16px; }
.bar-label { width: 80px; color: #606266; }
.bar-track { flex: 1; height: 18px; background: #f0f2f5; border-radius: 9px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 9px; transition: width .4s; }
.bar-num { width: 40px; text-align: right; color: #303133; font-weight: 600; }
</style>
