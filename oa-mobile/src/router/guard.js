import router from './index'
import { useUserStore } from '@/store/user'
import { getInfo } from '@/api'

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') return next()
  if (!userStore.token) return next('/login')
  if (!userStore.nickname) {
    try { const res = await getInfo(); userStore.setInfo(res.data) } catch (e) { userStore.reset(); return next('/login') }
  }
  next()
})
