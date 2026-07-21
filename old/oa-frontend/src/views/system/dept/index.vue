<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="名称"><el-input v-model="name" clearable @keyup.enter="load" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="load">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd()" v-if="user.hasPerm('system:dept:add')">新增</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all>
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('system:dept:add')" link type="primary" @click="openAdd(row.id)">新增子级</el-button>
            <el-button v-if="user.hasPerm('system:dept:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('system:dept:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑部门' : '新增部门'" width="600px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="90px">
        <el-form-item label="上级部门"><el-cascader v-model="form.data.parentId" :options="options" :props="casc" :show-all-levels="false" style="width:100%" /></el-form-item>
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.data.deptName" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.data.leader" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.data.sort" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.data.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.data.email" /></el-form-item></el-col>
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
import { deptTree, getDept, addDept, updateDept, deleteDept } from '@/api/system'

const user = useUserStore()
const loading = ref(false); const name = ref(''); const tree = ref([])
const formRef = ref(); const form = reactive({ visible: false, data: {} })
const options = ref([]); const casc = { checkStrictly: true, emitPath: false, value: 'id', label: 'deptName', children: 'children' }
const rules = { deptName: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try { const res = await deptTree({ deptName: name.value, status: 0 }); tree.value = res.data || []; options.value = [{ id: 0, deptName: '顶层', children: res.data || [] }] }
  finally { loading.value = false }
}
function openAdd(parentId) { form.data = { parentId: parentId || 0, sort: 0, status: 0 }; form.visible = true }
async function openEdit(row) { const res = await getDept(row.id); form.data = res.data; form.visible = true }
async function save() {
  await formRef.value.validate()
  if (form.data.id) await updateDept(form.data); else await addDept(form.data)
  ElMessage.success('保存成功'); form.visible = false; load()
}
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await deleteDept(row.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
