<template>
  <div class="login">
    <div class="banner">
      <div class="logo">协同办公</div>
      <div class="sub">移动办公 · 随时随地</div>
    </div>
    <van-form @submit="onLogin" class="form">
      <van-cell-group inset>
        <van-field v-model="form.username" label="账号" placeholder="请输入账号" :rules="[{ required: true }]" />
        <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" :rules="[{ required: true }]" />
        <van-field v-model="form.code" label="验证码" placeholder="验证码" center :rules="[{ required: true }]" v-if="captcha.enabled">
          <template #button>
            <img v-if="captcha.img" :src="captcha.img" class="cap" @click="loadCaptcha" />
          </template>
        </van-field>
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">登 录</van-button>
      </div>
      <p class="tip">默认：admin / admin123</p>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'
import { login, getCaptcha } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const captcha = reactive({ enabled: false, img: '', uuid: '' })
const form = reactive({ username: 'admin', password: 'admin123', code: '' })

async function loadCaptcha() {
  const res = await getCaptcha()
  captcha.enabled = res.data.captchaEnabled
  captcha.img = res.data.img
  captcha.uuid = res.data.uuid
  form.uuid = res.data.uuid
}
async function onLogin() {
  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.data.token)
    showToast('登录成功')
    router.replace('/')
  } catch (e) {
    loadCaptcha()
  } finally { loading.value = false }
}
onMounted(loadCaptcha)
</script>

<style scoped>
.login { min-height: 100vh; background: #fff; }
.banner { background: linear-gradient(135deg, #1989fa, #07c160); color: #fff; padding: 60px 24px 40px; }
.logo { font-size: 28px; font-weight: 700; }
.sub { margin-top: 8px; opacity: .9; }
.form { margin-top: 24px; }
.cap { width: 100px; height: 36px; }
.tip { text-align: center; color: #969799; font-size: 12px; }
</style>
