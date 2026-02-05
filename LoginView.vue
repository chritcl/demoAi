<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="login-content">
      <div class="login-left">
        <div class="system-header">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <img src="@/assets/logo.svg" alt="Logo" />
            </div>
          </div>
          <h1 class="system-title">农业财政专项资金系统</h1>
        </div>

        <div class="feature-cards">
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>资金监管</h3>
            <p>全流程资金追踪与监管</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📈</div>
            <h3>数据分析</h3>
            <p>智能化数据分析与决策支持</p>
          </div>
        </div>

        <div class="system-info">
          <div class="info-item">
            <span class="info-icon">🔒</span>
            <span>安全可靠 · 数据加密</span>
          </div>
          <div class="info-item">
            <span class="info-icon">⚡</span>
            <span>高效便捷 · 智能管理</span>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-card">
          <div class="card-header">
            <h2 class="card-title">系统登录</h2>
          </div>

          <div class="login-form-wrapper">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" class="login-form">
              <el-form-item prop="username" label="用户名">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" @keyup.enter="handleLogin">
                  <template #prefix>
                    <el-icon>
                      <User />
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item prop="password" label="密码">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password
                  @keyup.enter="handleLogin">
                  <template #prefix>
                    <el-icon>
                      <Lock />
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <div class="login-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              </div>
              <el-form-item>
                <button type="button" class="custom-login-button" :class="{ 'is-loading': loading }"
                  @click="handleLogin">
                  <span class="dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                  </span>
                  <span class="button-text">登录系统</span>
                  <span class="button-icon">
                    <el-icon>
                      <ArrowRight />
                    </el-icon>
                  </span>
                  <span class="loading-spinner" v-if="loading">
                    <svg class="circular" viewBox="25 25 50 50">
                      <circle class="path" cx="50" cy="50" r="20" fill="none" />
                    </svg>
                  </span>
                  <div class="particles">
                    <div class="particle" v-for="i in 20" :key="i"></div>
                  </div>
                </button>
              </el-form-item>
            </el-form>
          </div>

          <div class="login-footer">
            <!-- <p>技术支持：福州市农业农村局信息中心</p> -->
            <p>技术支持：XXXX</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Lock, User, ArrowRight } from '@element-plus/icons-vue'
import { getMenuButtonAndRolePermission, getMyAreas, login } from '@/api/modules/common/common'
import { AreaInfoCookie } from '@/stores/areaInfoCookie'
import { useAuthStore } from '@/stores/auth'
import { useMenuStore } from '@/stores/menu'
import crypto from '@/utils/crypto'
import { ElLoading } from 'element-plus'
import { customLoadingSvg } from '@/api/utils/http'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const loginRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' },
  ],
})

const password = 'Leadingtv00@123'

const handleLogin = async () => {
  if (!loginFormRef.value) return

  if (rememberMe) {
    useAuthStore().setRememberMe(true)
    useAuthStore().setLoginUsername(loginForm.username)
    useAuthStore().setLoginPassword(crypto.encrypt(loginForm.password, password))
  } else {
    useAuthStore().setRememberMe(false)
    useAuthStore().setLoginUsername('')
    useAuthStore().setLoginPassword('')
  }

  useAuthStore().clearAuth()

  const menuStore = useMenuStore()
  menuStore.reset()

  const areaInfoCookie = new AreaInfoCookie

  areaInfoCookie.removeAreaInfo()

  await loginFormRef.value.validate((valid, fields) => {
    if (valid) {
      loading.value = true

      // 请求登录接口
      login(loginForm).then(async (res) => {
        if (res.code === '000') {
          useAuthStore().setToken(res.data?.data?.tokenInfoVo?.token)

          useAuthStore().setUserInfo(res.data?.data?.userVo)
          useAuthStore().setResServerUrl(res.resServerUrl)

          useAuthStore().setDepartmentInfo(res.data?.data?.departList)


          const lastSelectedDepartInfo = useAuthStore().getlastSelectedDepartInfoById(res.data?.data?.userVo?.id!)
          if (lastSelectedDepartInfo) {
            useAuthStore().setlastSelectedDepartInfo({
              userId: res.data?.data?.userVo?.id!,
              areaId: lastSelectedDepartInfo.areaId,
              departId: lastSelectedDepartInfo.departId
            })
            areaInfoCookie.setAreaInfo({ id: lastSelectedDepartInfo.areaId })
          } else {
            useAuthStore().setlastSelectedDepartInfo({
              userId: res.data?.data?.userVo?.id!,
              areaId: res.data?.data?.departList[res.data?.data?.departList.length - 1].areaId,
              departId: res.data?.data?.departList[res.data?.data?.departList.length - 1].id
            })
            areaInfoCookie.setAreaInfo({ id: res.data?.data?.departList[res.data?.data?.departList.length - 1].areaId })
          }

          await useMenuStore().loadMenus()

          // 初始化菜单（防止菜单未初始化）
          if (Object.keys(menuStore.sideMenus).length === 0) {
            // console.log('菜单未初始化，正在初始化...')
            menuStore.initMenuFromRoutes()
          }

          // 获取菜单按钮及角色权限
          getMenuButtonAndRolePermission().then((res) => {
            if (res.code === '000') {
              useAuthStore().setMenuButtonAndRolePermission(res.data.permissionCodeList)
            } else {
              ElMessage.error({ showClose: true, message: res.message, plain: true })
            }
          })

          ElMessage.success({ showClose: true, message: '登录成功', plain: true })

          // ✅ 再跳转
          router.push('/systemOverview')
        } else {
          ElMessage.error({ showClose: true, message: '用户名或密码错误', plain: true })

        }
      }).catch((error: any) => {
        console.error('登录出错', error)
        ElMessage.error({ showClose: true, message: '登录错误', plain: true })
      }).finally(() => {
        setTimeout(() => {
          loading.value = false
        }, 500)
      })
    }
  })
}

onMounted(() => {
  if (useAuthStore().getRememberMe()) {
    loginForm.username = useAuthStore().getLoginUsername()
    loginForm.password = crypto.decrypt(useAuthStore().getLoginPassword(), password)
    rememberMe.value = true
  } else {
    rememberMe.value = false
  }
  const menuStore = useMenuStore()
  menuStore.reset()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 50%, #93c5fd 100%);
  position: relative;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.bg-circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation: float 20s infinite ease-in-out;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  right: -80px;
  animation: float 15s infinite ease-in-out reverse;
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: 10%;
  animation: float 18s infinite ease-in-out;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }

  50% {
    transform: translateY(-30px) rotate(5deg);
  }
}

.login-content {
  display: flex;
  width: 100%;
  max-width: 1300px;
  height: 650px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(30, 64, 175, 0.15);
  position: relative;
  z-index: 1;
}

/* 左侧区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1e40af 0%, #2563eb 50%, #3b82f6 100%);
  padding: 40px 50px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.3;
}

.system-header {
  position: relative;
  z-index: 1;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-icon {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.486);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.logo-icon svg {
  width: 45px;
  height: 45px;
}

.system-title {
  font-size: 34px;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  margin: 0;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.feature-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  margin: 30px 0 20px 0;
  position: relative;
  z-index: 1;
}

.feature-card {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  padding: 18px 20px;
  transition: all 0.3s ease;
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateX(8px);
}

.feature-icon {
  font-size: 28px;
  margin-bottom: 8px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.feature-card h3 {
  font-size: 16px;
  color: #ffffff;
  margin: 0 0 6px 0;
  font-weight: 600;
}

.feature-card p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
}

.system-info {
  display: flex;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.info-icon {
  font-size: 20px;
}

/* 右侧登录区域 */
.login-right {
  width: 500px;
  background: #ffffff;
  padding: 50px 45px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-card {
  width: 100%;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  font-size: 26px;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.login-form-wrapper {
  margin-bottom: 24px;
}

.login-form {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.login-footer p {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 登录按钮 - 保留所有动画效果，更新配色 */
.custom-login-button {
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(45deg, #1e40af, #2563eb, #3b82f6);
  background-size: 200% 100%;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(30, 64, 175, 0.3);
  padding: 0 20px;
  letter-spacing: 1px;
}

.custom-login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 64, 175, 0.4);
  background-position: 100% 0;
}

.custom-login-button:active {
  transform: translateY(1px);
  box-shadow: 0 2px 10px rgba(30, 64, 175, 0.3);
}

.custom-login-button .button-text {
  flex: 1;
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.custom-login-button .button-icon {
  position: absolute;
  right: 25px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
  z-index: 2;
}

.custom-login-button:hover .button-icon {
  opacity: 1;
  transform: translateX(0);
}

.custom-login-button .dots {
  position: absolute;
  left: 25px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
}

.custom-login-button:hover .dots {
  opacity: 1;
  transform: translateX(0);
}

.custom-login-button .dot {
  width: 6px;
  height: 6px;
  background-color: white;
  border-radius: 50%;
  display: inline-block;
  opacity: 0;
  animation: fadeInOut 1.5s infinite;
}

.custom-login-button .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.custom-login-button .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
  }

  50% {
    opacity: 1;
  }

  100% {
    opacity: 0;
  }
}

.custom-login-button.is-loading {
  pointer-events: none;
  opacity: 0.9;
}

.custom-login-button.is-loading .button-text,
.custom-login-button.is-loading .button-icon,
.custom-login-button.is-loading .dots {
  opacity: 0;
}

.loading-spinner {
  display: inline-block;
  position: relative;
  width: 24px;
  height: 24px;
}

.circular {
  animation: rotate 2s linear infinite;
  height: 100%;
  width: 100%;
}

.path {
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 3;
  stroke: #fff;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }

  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }

  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #2563eb;
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.1);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #1e40af;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

:deep(.el-input__prefix) {
  color: #666;
  font-size: 16px;
}

:deep(.el-input__inner) {
  font-size: 15px;
}

:deep(.el-checkbox__label) {
  font-size: 14px;
  color: #666;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #1e40af;
  border-color: #1e40af;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #1e40af;
}

/* 粒子爆炸效果 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  top: 50%;
  left: 50%;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
}

.custom-login-button:hover .particle {
  animation: explode var(--duration) cubic-bezier(0.25, 0.46, 0.45, 0.94) var(--delay) forwards;
}

@keyframes explode {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }

  50% {
    opacity: 1;
  }

  100% {
    transform: translate(-50%, -50%) scale(var(--scale, 1)) translate(var(--tx), var(--ty));
    opacity: 0;
  }
}

/* 粒子差异化配置：方向、大小、缩放、延迟、时长 */
.particle:nth-child(1) {
  --tx: 30px;
  --ty: -25px;
  --scale: 1.2;
  width: 5px;
  height: 5px;
  --delay: 0s;
  --duration: 0.6s;
}

.particle:nth-child(2) {
  --tx: -25px;
  --ty: 30px;
  --scale: 0.8;
  width: 3px;
  height: 3px;
  --delay: 0.1s;
  --duration: 0.8s;
}

.particle:nth-child(3) {
  --tx: 35px;
  --ty: 40px;
  --scale: 1.1;
  width: 4px;
  height: 4px;
  --delay: 0.05s;
  --duration: 0.7s;
}

.particle:nth-child(4) {
  --tx: -40px;
  --ty: -30px;
  --scale: 0.9;
  width: 6px;
  height: 6px;
  --delay: 0.2s;
  --duration: 0.9s;
}

.particle:nth-child(5) {
  --tx: 15px;
  --ty: 35px;
  --scale: 1.3;
  width: 3px;
  height: 3px;
  --delay: 0s;
  --duration: 0.8s;
}

.particle:nth-child(6) {
  --tx: -35px;
  --ty: 20px;
  --scale: 0.7;
  width: 5px;
  height: 5px;
  --delay: 0.15s;
  --duration: 0.6s;
}

.particle:nth-child(7) {
  --tx: 25px;
  --ty: -35px;
  --scale: 1.0;
  width: 4px;
  height: 4px;
  --delay: 0.05s;
  --duration: 0.75s;
}

.particle:nth-child(8) {
  --tx: -30px;
  --ty: 40px;
  --scale: 1.2;
  width: 3px;
  height: 3px;
  --delay: 0.1s;
  --duration: 0.85s;
}

.particle:nth-child(9) {
  --tx: 40px;
  --ty: -20px;
  --scale: 0.8;
  width: 5px;
  height: 5px;
  --delay: 0.2s;
  --duration: 0.65s;
}

.particle:nth-child(10) {
  --tx: -15px;
  --ty: -40px;
  --scale: 1.1;
  width: 4px;
  height: 4px;
  --delay: 0s;
  --duration: 0.9s;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .login-content {
    max-width: 1000px;
    height: 600px;
  }

  .login-left {
    padding: 35px 40px;
  }

  .system-title {
    font-size: 30px;
  }

  .login-right {
    padding: 40px 35px;
  }
}

@media (max-width: 992px) {
  .login-content {
    flex-direction: column;
    max-width: 600px;
    height: auto;
    max-height: 95vh;
    overflow-y: auto;
  }

  .login-left {
    padding: 30px;
  }

  .login-right {
    width: 100%;
    padding: 30px;
  }

  .feature-cards {
    grid-template-columns: 1fr;
    gap: 10px;
    margin: 20px 0 15px 0;
  }
}

@media (max-width: 576px) {
  .system-title {
    font-size: 24px;
  }

  .login-right {
    padding: 25px 20px;
  }

  .card-title {
    font-size: 22px;
  }

  .logo-icon {
    width: 60px;
    height: 60px;
  }
}
</style>
