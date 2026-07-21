<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="名称"><el-input v-model="name" clearable @keyup.enter="load" /></el-form-item>
        <el-form-item><el-button type="primary" :icon="'Search'" @click="load">查询</el-button>
          <el-button type="primary" :icon="'Plus'" @click="openAdd()" v-if="user.hasPerm('system:menu:add')">新增</el-button></el-form-item>
      </el-form>
      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all>
        <el-table-column prop="menuName" label="名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="80"><template #default="{ row }"><el-icon><component :is="resolveIconName(row.icon)" /></el-icon></template></el-table-column>
        <el-table-column prop="menuType" label="类型" width="80"><template #default="{ row }">{{ { M: '目录', C: '菜单', F: '按钮' }[row.menuType] }}</template></el-table-column>
        <el-table-column prop="path" label="路径" width="140" />
        <el-table-column prop="component" label="组件" width="180" />
        <el-table-column prop="perms" label="权限" width="180" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="user.hasPerm('system:menu:add')" link type="primary" @click="openAdd(row.id)">新增子级</el-button>
            <el-button v-if="user.hasPerm('system:menu:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="user.hasPerm('system:menu:remove')" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="form.visible" :title="form.data.id ? '编辑菜单' : '新增菜单'" width="720px">
      <el-form ref="formRef" :model="form.data" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12"><el-form-item label="上级菜单"><el-cascader v-model="form.data.parentId" :options="options" :props="casc" :show-all-levels="false" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-radio-group v-model="form.data.menuType"><el-radio value="M">目录</el-radio><el-radio value="C">菜单</el-radio><el-radio value="F">按钮</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称" prop="menuName"><el-input v-model="form.data.menuName" /></el-form-item></el-col>
          <el-col :span="12" v-if="form.data.menuType !== 'F'"><el-form-item label="图标"><el-input v-model="form.data.icon" placeholder="如 user/menu" /></el-form-item></el-col>
          <el-col :span="12" v-if="form.data.menuType !== 'F'"><el-form-item label="路径"><el-input v-model="form.data.path" /></el-form-item></el-col>
          <el-col :span="12" v-if="form.data.menuType === 'C'"><el-form-item label="组件"><el-input v-model="form.data.component" placeholder="如 system/user/index" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="权限标识"><el-input v-model="form.data.perms" placeholder="如 system:user:add" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.data.sort" :min="0" /></el-form-item></el-col>
          <el-col :span="12" v-if="form.data.menuType !== 'F'"><el-form-item label="显示"><el-radio-group v-model="form.data.visible"><el-radio :value="0">显示</el-radio><el-radio :value="1">隐藏</el-radio></el-radio-group></el-form-item></el-col>
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
import { resolveIconName } from '@/utils/icons'
import { listMenu, getMenu, addMenu, updateMenu, deleteMenu } from '@/api/system'

const user = useUserStore()
const loading = ref(false); const name = ref(''); const tree = ref([])
const formRef = ref(); const form = reactive({ visible: false, data: {} })
const options = ref([]); const casc = { checkStrictly: true, emitPath: false, value: 'id', label: 'menuName', children: 'children' }
const rules = { menuName: [{ required: true, message: '必填', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try { const res = await listMenu({ menuName: name.value }); tree.value = buildTree(res.data || []); options.value = [{ id: 0, menuName: '顶层', children: buildTree(res.data || []) }] }
  finally { loading.value = false }
}
function buildTree(nodes) {
  const map = {}; nodes.forEach((n) => { map[n.id] = { ...n, children: [] } })
  const t = []
  nodes.forEach((n) => { if (n.parentId && map[n.parentId]) map[n.parentId].children.push(map[n.id]); else t.push(map[n.id]) })
  return t
}
function openAdd(parentId) { form.data = { menuType: 'C', visible: 0, isFrame: 1, isCache: 0, sort: 0, status: 0, parentId: parentId || 0 }; form.visible = true }
async function openEdit(row) { const res = await getMenu(row.id); form.data = res.data; form.visible = true }
async function save() {
  await formRef.value.validate()
  if (form.data.id) await updateMenu(form.data); else await addMenu(form.data)
  ElMessage.success('保存成功'); form.visible = false; load()
}
async function doDelete(row) { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await deleteMenu(row.id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
