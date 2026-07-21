<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :md="8">
        <el-card shadow="never" class="clock-card">
          <div class="clock-time">{{ now }}</div>
          <div class="clock-date">{{ dateStr }}</div>
          <div class="clock-status">
            <div>上班打卡：<el-tag :type="today.clockIn ? 'success' : 'info'" size="small">{{ today.clockIn || '未打卡' }}</el-tag></div>
            <div style="margin-top:8px">下班打卡：<el-tag :type="today.clockOut ? 'success' : 'info'" size="small">{{ today.clockOut || '未打卡' }}</el-tag></div>
          </div>
          <div class="clock-btns">
            <el-button type="primary" size="large" @click="doClock('in')" :disabled="!!today.clockIn">上班打卡</el-button>
            <el-button type="success" size="large" @click="doClock('out')" :disabled="!today.clockIn || !!today.clockOut">下班打卡</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :md="16">
        <el-card shadow="never" header="考勤统计">
          <div class="stat-row">
            <div class="stat" v-for="s in stats" :key="s.key"><div class="num">{{ statData[s.key] || 0 }}</div><div class="lbl">{{ s.label }}</div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <span>我的考勤记录</span>
        <el-input v-model="month" placeholder="月份(YYYY-MM)" style="width:160px;margin-left:12px" @keyup.enter="loadRecords" />
      </template>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="attDate" label="日期" width="130" />
        <el-table-column prop="clockIn" label="上班打卡" width="120" />
        <el-table-column prop="clockOut" label="下班打卡" width="120" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="typeOf(row.status)" size="small">{{ dictLabel('oa_attendance_status', row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dictLabel } from '@/composables/useDict'
import { attendance as api } from '@/api/office'

const now = ref(''); const dateStr = ref('')
const today = reactive({ clockIn: null, clockOut: null })
const statData = reactive({})
const stats = [
  { key: 'normal', label: '正常' }, { key: 'late', label: '迟到' },
  { key: 'earlyLeave', label: '早退' }, { key: 'absent', label: '缺勤' }
]
const month = ref('')
const list = ref([]); const loading = ref(false)
let timer

function tick() {
  const d = new Date()
  now.value = d.toTimeString().substring(0, 8)
  const w = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  dateStr.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${w}`
}
async function loadToday() { const res = await api.today(); if (res.data) { today.clockIn = res.data.clockIn; today.clockOut = res.data.clockOut } }
async function loadStats() { const res = await api.statistics(); Object.assign(statData, res.data || {}) }
async function loadRecords() { loading.value = true; try { const res = await api.my({ pageNum: 1, pageSize: 50, month: month.value }); list.value = res.data.list } finally { loading.value = false } }
async function doClock(t) {
  if (t === 'in') { const res = await api.clockIn(); today.clockIn = res.data.clockIn; ElMessage.success('上班打卡成功') }
  else { const res = await api.clockOut(); today.clockOut = res.data.clockOut; ElMessage.success('下班打卡成功') }
  loadStats(); loadRecords()
}
function typeOf(s) { return { normal: 'success', late: 'warning', earlyLeave: 'warning', absent: 'danger' }[s] || 'info' }

onMounted(() => { tick(); timer = setInterval(tick, 1000); loadToday(); loadStats(); loadRecords() })
onUnmounted(() => clearInterval(timer))
</script>

<style scoped lang="scss">
.clock-card { text-align: center; }
.clock-time { font-size: 36px; font-weight: 700; color: #409eff; }
.clock-date { color: #909399; margin-top: 4px; }
.clock-status { margin: 20px 0; line-height: 1.8; }
.clock-btns { display: flex; gap: 12px; justify-content: center; }
.stat-row { display: flex; justify-content: space-around; }
.stat { text-align: center; }
.stat .num { font-size: 28px; font-weight: 700; color: #409eff; }
.stat .lbl { color: #909399; font-size: 13px; margin-top: 4px; }
</style>
