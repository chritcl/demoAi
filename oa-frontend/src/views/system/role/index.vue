<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="角色名"><el-input v-model="query.roleName" clearable @keyup.enter="onSearch" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="onSearch">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd" v-if="user.hasPerm('system:role:add')">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleKey" label="编码" width="160" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '正常' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170"><template #default="{ row }">{{ fmt(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('system:role:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('system:role:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @load="load" />
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑角色' : '新增角色'" width="720px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12"><el-form-item label="角色名" prop="roleName"><el-input v-model="form.data.roleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="编码" prop="roleKey"><el-input v-model="form.data.roleKey" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.data.sort" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-radio-group v-model="form.data.status"><el-radio :value="0">正常</el-radio><el-radio :value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="菜单授权">
          <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" :props="{ label: 'menuName', children: 'children' }" default-expand-all />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="form.visible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageRole, getRole, addRole, updateRole, deleteRole } from '@/api/system'
import { listMenu } from '@/api/system'
import Pagination from '@/components/Pagination/index.vue'

const user = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, roleName: '' })
const formRef = ref(); const treeRef = ref()
const form = reactive({ visible: false, data: {} })
const menuTree = ref([])
const rules = { roleName: [{ required: true, message: '必填', trigger: 'blur' }], roleKey: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() { loading.value = true; try { const res = await pageRole(query); list.value = res.data.list; total.value = res.data.total } finally { loading.value = false } }
function onSearch() { query.pageNum = 1; load() }
function buildTree(nodes) {
  const map = {}; nodes.forEach((n) => { map[n.id] = { ...n, children: [] } })
  const tree = []
  nodes.forEach((n) => { if (n.parentId && map[n.parentId]) map[n.parentId].children.push(map[n.id]); else tree.push(map[n.id]) })
  return tree
}
async function openAdd() { form.data = { sort: 0, status: 0 }; form.visible = true; await loadMenuTree(); setTimeout(() => treeRef.value && treeRef.value.setCheckedKeys([])) }
async function openEdit(row) {
  const res = await getRole(row.id); form.data = res.data
  await loadMenuTree(); form.visible = true
  setTimeout(() => treeRef.value && treeRef.value.setCheckedKeys(res.data.menuIds || []))
}
async function loadMenuTree() {
  const res = await listMenu(); menuTree.value = buildTree(res.data || [])
}
async function save() {
  await formRef.value.validate()
  form.data.menuIds = treeRef.value ? treeRef.value.getCheckedKeys().concat(treeRef.value.getHalfCheckedKeys()) : []
  if (form.data.id) await updateRole(form.data); else await addRole(form.data)
  ElMessage.success('保存成功'); form.visible = false; load()
}
async function doDelete(row) { await ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' }); await deleteRole(row.id); ElMessage.success('已删除'); load() }
function fmt(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' }
onMounted(load)
</script>
