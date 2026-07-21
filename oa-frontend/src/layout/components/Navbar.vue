<template>
  <div class="navbar">
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="toggle"><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
      <breadcrumb />
    </div>
    <div class="navbar-right">
      <el-badge :value="unread" :hidden="unread === 0" :max="99">
        <el-icon class="icon-btn" @click="goMessage"><component :is="'Bell'" /></el-icon>
      </el-badge>
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="28" :src="avatar">{{ nickname.charAt(0) }}</el-avatar>
          <span class="username">{{ nickname }}</span>
          <el-icon><component :is="'ArrowDown'" /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="pwdForm.confirm" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { unreadCount } from '@/api/portal'
import { updatePassword } from '@/api/system'
import Breadcrumb from './Breadcrumb.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const nickname = computed(() => userStore.nickname || '用户')
const avatar = computed(() => userStore.avatar)
const unread = ref(0)

function toggle() {
  appStore.toggleSidebar()
}

function goMessage() {
  router.push('/message')
}

async function loadUnread() {
  try {
    const res = await unreadCount()
    unread.value = res.data || 0
  } catch (e) {}
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(async () => {
      await userStore.logout()
      router.push('/login')
    })
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'password') {
    pwdVisible.value = true
  }
}

const pwdVisible = ref(false)
const pwdRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirm: [{ required: true, message: '请确认密码', trigger: 'blur' }, {
    validator: (r, v, cb) => (v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致'))), trigger: 'blur'
  }]
}
async function submitPwd() {
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return
    await updatePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('修改成功，请重新登录')
    pwdVisible.value = false
    await userStore.logout()
    router.push('/login')
  })
}

onMounted(loadUnread)
defineExpose({ loadUnread })
</script>

<style scoped lang="scss">
.navbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.navbar-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: #5a5e66; }
.navbar-right { display: flex; align-items: center; gap: 20px; }
.icon-btn { font-size: 18px; cursor: pointer; color: #5a5e66; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; }
</style>
