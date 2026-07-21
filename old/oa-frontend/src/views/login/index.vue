<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="brand">
          <h1>协同办公平台</h1>
          <p>Collaborative Office Platform</p>
          <ul class="features">
            <li>统一门户 · 工作台 · 一站式办公</li>
            <li>公文管理 · 流程审批 · 信息发布</li>
            <li>通讯录 · 综合办公 · 考勤管理</li>
          </ul>
        </div>
      </div>
      <div class="login-right">
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <h2 class="title">账号登录</h2>
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password
              placeholder="请输入密码" :prefix-icon="Lock" @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item prop="code" v-if="captcha.enabled">
            <div class="captcha-row">
              <el-input v-model="form.code" size="large" placeholder="验证码" :prefix-icon="Key" @keyup.enter="handleLogin" />
              <img v-if="captcha.img" :src="captcha.img" class="captcha-img" @click="loadCaptcha" alt="验证码" />
            </div>
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
          <div class="tips">默认账号：admin / <REDACTED> &nbsp;|&nbsp; zhangsan / <REDACTED></div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const captcha = reactive({ enabled: false, img: '', uuid: '' })

const form = reactive({ username: 'admin', password: '<REDACTED_DEFAULT_PASSWORD>', code: '', uuid: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadCaptcha() {
  const res = await getCaptcha()
  captcha.enabled = res.data.captchaEnabled
  captcha.img = res.data.img
  captcha.uuid = res.data.uuid
  form.uuid = res.data.uuid
}

async function handleLogin() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } catch (e) {
      loadCaptcha()
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}
.login-box {
  width: 900px;
  height: 520px;
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #2a5298, #1e3c72);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  .brand {
    padding: 40px;
    h1 { font-size: 30px; margin: 0 0 8px; }
    p { opacity: 0.8; margin: 0 0 30px; }
    .features { list-style: none; padding: 0; opacity: 0.9;
      li { line-height: 2; }
    }
  }
}
.login-right {
  width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-form {
  width: 320px;
  .title { text-align: center; margin-bottom: 30px; color: #303133; }
}
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-img { width: 110px; height: 40px; border-radius: 4px; cursor: pointer; border: 1px solid #dcdfe6; }
.login-btn { width: 100%; margin-top: 6px; }
.tips { margin-top: 16px; text-align: center; font-size: 12px; color: #909399; }
</style>

