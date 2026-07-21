import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/dev-api',
  timeout: 30000
})

service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) config.headers['Authorization'] = 'Bearer ' + userStore.token
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.reset()
      router.replace('/login')
    }
    showToast(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'error'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.reset()
      router.replace('/login')
    } else {
      showToast(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
