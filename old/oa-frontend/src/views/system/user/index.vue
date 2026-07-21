<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="用户名"><el-input v-model="query.username" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="query.phone" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable style="width:110px"><el-option label="正常" :value="0" /><el-option label="停用" :value="1" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('system:user:add')">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="姓名" width="110" />
        <el-table-column prop="deptName" label="部门" width="120"><template #default="{ row }">{{ deptName(row.deptId) }}</template></el-table-column>
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 0" @change="(v) => toggleStatus(row, v ? 0 : 1)" :disabled="!user.hasPerm('system:user:edit')" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('system:user:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('system:user:resetPwd')" link type="warning" @click="resetPwd(row)">重置密码</el-button>
            <el-button v-if="user.hasPerm('system:user:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑用户' : '新增用户'" width="640px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12"><el-form-item label="用户名" prop="username"><el-input v-model="form.data.username" :disabled="!!form.data.id" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="nickname"><el-input v-model="form.data.nickname" /></el-form-item></el-col>
          <el-col :span="12" v-if="!form.data.id"><el-form-item label="密码" prop="password"><el-input v-model="form.data.password" type="password" show-password /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门"><el-cascader v-model="form.data.deptId" :options="deptOpts" :props="cascaderProps" :show-all-levels="false" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.data.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.data.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别"><el-radio-group v-model="form.data.gender"><el-radio :value="0">男</el-radio><el-radio :value="1">女</el-radio><el-radio :value="2">未知</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="角色"><el-select v-model="form.data.roleIds" multiple style="width:100%"><el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="form.visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageUser, getUser, getUserRoles, addUser, updateUser, deleteUser, resetUserPwd, changeUserStatus } from '@/api/system'
import { deptTree } from '@/api/system'
import { roleOptions } from '@/api/system'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', phone: '', status: undefined })
const formRef = ref()
const form = reactive({ visible: false, data: {} })
const roles = ref([]); const deptOpts = ref([]); const deptList = ref([])
const cascaderProps = { checkStrictly: true, emitPath: false, value: 'id', label: 'deptName', children: 'children' }
const rules = {
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  nickname: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }]
}

function deptName(id) { const d = deptList.value.find((x) => x.id === id); return d ? d.deptName : '-' }

async function load() {
  loading.value = true
  try { const res = await pageUser(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false }
}
function onSearch() { query.pageNum = 1; load() }
function reset() { query.username = ''; query.phone = ''; query.status = undefined; onSearch() }

async function openAdd() { form.data = { gender: 0, status: 0, roleIds: [], password: '123456' }; form.visible = true }
async function openEdit(row) {
  const [d, r] = await Promise.all([getUser(row.id), getUserRoles(row.id)])
  form.data = { ...d.data, roleIds: r.data || [] }
  form.visible = true
}
async function save() {
  await formRef.value.validate()
  if (form.data.id) await updateUser(form.data); else await addUser(form.data)
  ElMessage.success('保存成功'); form.visible = false; load()
}
async function doDelete(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await deleteUser(row.id); ElMessage.success('已删除'); load()
}
async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt(`重置 ${row.username} 的密码`, '重置密码', { inputPattern: /.{6,}/, inputErrorMessage: '至少6位' })
  await resetUserPwd(row.id, value); ElMessage.success('已重置')
}
async function toggleStatus(row, status) { await changeUserStatus(row.id, status); row.status = status }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }

function flattenDept(nodes, arr) { nodes.forEach((n) => { arr.push(n); if (n.children) flattenDept(n.children, arr) }); return arr }

onMounted(async () => {
  load()
  roles.value = (await roleOptions()).data || []
  const dres = await deptTree({ status: 0 }); deptOpts.value = dres.data || []; deptList.value = flattenDept(deptOpts.value, [])
})
</script>
