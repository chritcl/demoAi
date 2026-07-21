<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>流程定义</span></template>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="flowName" label="流程名称" min-width="160" />
        <el-table-column prop="flowKey" label="标识" width="150" />
        <el-table-column prop="businessType" label="业务类型" width="150" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">{{ row.status === 0 ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="140"><template #default="{ row }"><el-button link type="primary" @click="open(row)">配置节点</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg.visible" title="流程节点配置" width="720px">
      <div class="node-head">流程：<b>{{ dlg.data.flowName }}</b></div>
      <el-table :data="dlg.data.nodes" border size="small">
        <el-table-column type="index" label="序" width="50" align="center" />
        <el-table-column label="环节名称" width="180">
          <template #default="{ row }"><el-input v-model="row.nodeName" size="small" /></template>
        </el-table-column>
        <el-table-column label="审批人类型" width="150">
          <template #default="{ row }">
            <el-select v-model="row.approverType" size="small">
              <el-option label="指定用户" value="user" /><el-option label="角色" value="role" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="审批人/角色">
          <template #default="{ row }">
            <el-select v-if="row.approverType === 'user'" v-model="row.approverValue" size="small" filterable>
              <el-option v-for="u in users" :key="u.id" :label="u.nickname" :value="String(u.id)" />
            </el-select>
            <el-select v-else v-model="row.approverValue" size="small">
              <el-option v-for="r in roles" :key="r.roleKey" :label="r.roleName" :value="r.roleKey" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }"><el-button link type="danger" @click="dlg.data.nodes.splice($index,1)">删</el-button></template>
        </el-table-column>
      </el-table>
      <el-button :icon="'Plus'" style="margin-top:8px" @click="addNode">添加节点</el-button>
      <template #footer>
        <el-button @click="dlg.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listFlowDefinition, getFlowDefinition, saveFlowDefinition } from '@/api/flow'
import { pageUser, roleOptions } from '@/api/system'

const list = ref([]); const loading = ref(false)
const users = ref([]); const roles = ref([])
const dlg = reactive({ visible: false, data: {} })

async function load() {
  loading.value = true
  try { list.value = (await listFlowDefinition()).data || [] } finally { loading.value = false }
}
async function loadRefs() {
  users.value = (await pageUser({ pageNum: 1, pageSize: 200 })).data.list
  roles.value = (await roleOptions()).data || []
}
async function open(row) {
  const res = await getFlowDefinition(row.id); dlg.data = res.data; if (!dlg.data.nodes) dlg.data.nodes = []; dlg.visible = true
}
function addNode() { dlg.data.nodes.push({ nodeName: '审批', approverType: 'user', approverValue: '1', sort: dlg.data.nodes.length + 1 }) }
async function save() {
  dlg.data.nodes.forEach((n, i) => (n.sort = i + 1))
  await saveFlowDefinition(dlg.data); ElMessage.success('保存成功'); dlg.visible = false; load()
}
onMounted(() => { load(); loadRefs() })
</script>

<style scoped>
.node-head { margin-bottom: 12px; }
</style>
