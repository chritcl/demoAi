<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :md="8">
        <el-card shadow="never">
          <div class="user-card">
            <el-avatar :size="80">{{ (form.nickname || 'U').charAt(0) }}</el-avatar>
            <h3>{{ form.nickname }}</h3>
            <div class="dept">{{ user.deptName }}</div>
            <el-tag v-for="r in user.roles" :key="r" size="small" style="margin:2px">{{ r }}</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :md="16">
        <el-card shadow="never" header="个人信息">
          <el-form ref="formRef" :model="form" label-width="90px" :rules="rules">
            <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
            <el-form-item label="姓名" prop="nickname"><el-input v-model="form.nickname" /></el-form-item>
            <el-form-item label="手机" prop="phone"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">男</el-radio><el-radio :value="1">女</el-radio><el-radio :value="2">未知</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="save">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getProfile, updateProfile } from '@/api/system'

const user = useUserStore()
const formRef = ref()
const form = reactive({ id: null, username: '', nickname: '', phone: '', email: '', gender: 2 })
const rules = {
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function load() {
  const res = await getProfile()
  Object.assign(form, res.data)
}
async function save() {
  await formRef.value.validate()
  await updateProfile(form)
  ElMessage.success('保存成功')
  user.nickname = form.nickname
}
onMounted(load)
</script>

<style scoped>
.user-card { text-align: center; padding: 10px 0; }
.user-card h3 { margin: 12px 0 4px; }
.dept { color: #909399; margin-bottom: 8px; }
</style>
