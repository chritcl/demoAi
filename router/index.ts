import { createRouter, createWebHistory } from 'vue-router'
import { menuRoutes } from './menu-config'
import { useAuthStore } from '@/stores/auth'
import { AreaInfoCookie } from '@/stores/areaInfoCookie'
import { useMenuStore } from '@/stores/menu'
import { ElLoading } from 'element-plus'
import { customLoadingSvg } from '@/api/utils/http'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: menuRoutes,
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
    const areaInfoCookie = new AreaInfoCookie()
    const token = useAuthStore().getToken()
    const loading = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
        customClass: 'custom-loading',
        spinner: customLoadingSvg,
    })

    // 如果访问根路径且已登录，重定向到dashboard
    if (to.path === '/' && token && areaInfoCookie.getAreaInfo() !== null) {
        loading.close()
        next({ path: '/dashboard' })
        return
    }

    // 如果访问catch-all路由，检查是否是动态路由未加载
    if (to.name === 'catch-all' && token && areaInfoCookie.getAreaInfo() !== null) {
        const menuStore = useMenuStore()
        const originalPath = Array.isArray(to.params.pathMatch) ?
            '/' + to.params.pathMatch.join('/') :
            '/' + (to.params.pathMatch || '')

        // 检查是否可能是动态路由（路径包含 dashboard/ 且不是静态路由）
        const possibleDynamicRoute = originalPath.includes('/dashboard/') &&
            !originalPath.includes('/systemOverview') &&
            !originalPath.includes('/personal-information')

        if (possibleDynamicRoute) {
            // 重置路由状态，强制重新加载
            menuStore.routesAdded = false
            menuStore.sideMenus = {}
            menuStore.menus = []
            menuStore.topMenus = []
            await menuStore.loadMenus()
            menuStore.initMenuFromRoutes()
            return next({ path: originalPath, replace: true })
        }
        loading.close()
        next()
        return
    }

    // 如果访问404页面，直接允许访问（无论是否登录）
    if (to.name === 'not-found') {
        // console.log('访问404页面，直接允许')
        loading.close()
        next()
        return
    }

    // 如果需要认证但未登录，重定向到登录页
    if (to.meta.requiresAuth && (!token || areaInfoCookie.getAreaInfo() === null || useMenuStore().topMenus.length === 0)) {
        loading.close()
        useAuthStore().clearAuth()
        useMenuStore().reset()
        next({ name: 'login' })
    }
    // 如果已登录且访问登录页，重定向到dashboard
    else if (token && areaInfoCookie.getAreaInfo() !== null && to.name === 'login') {
        loading.close()
        next({ path: '/dashboard' })
    } else {
        console.log('正常访问:', to.path)
        loading.close()
        next()
    }
})
export default router
