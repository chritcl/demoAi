import router from './index'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { getRouters } from '@/api/auth'
import { ElMessage } from 'element-plus'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/404']

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = (to.meta && to.meta.title ? to.meta.title + ' - ' : '') + '协同办公平台'
  const userStore = useUserStore()
  const hasToken = !!userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (userStore.roles && userStore.roles.length > 0) {
        next()
      } else {
        try {
          await userStore.fetchInfo()
          const res = await getRouters()
          const permStore = usePermissionStore()
          const accessRoutes = permStore.generateRoutes(res.data || [])
          accessRoutes.forEach((route) => router.addRoute(route))
          // 兜底 404
          router.addRoute({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
          next({ ...to, replace: true })
        } catch (err) {
          await userStore.logout()
          ElMessage.error(err.message || '获取用户信息失败')
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
