import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/dev-api',
  timeout: 30000
})

// 请求拦截：注入 token
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：统一处理 R 结构
let isRelogin = false
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    const code = res.code
    if (code === 200) {
      return res
    }
    if (code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(res.msg || '未登录'))
    }
    if (code === 403) {
      ElMessage.error(res.msg || '没有访问权限')
      return Promise.reject(new Error(res.msg || '没有访问权限'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      handleUnauthorized()
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  if (isRelogin) return
  isRelogin = true
  ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      const userStore = useUserStore()
      userStore.logout().finally(() => {
        router.push('/login')
        location.reload()
      })
    })
    .catch(() => {})
    .finally(() => {
      isRelogin = false
    })
}

export default service
