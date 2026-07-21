import { defineStore } from 'pinia'
import Layout from '@/layout/index.vue'
import ParentView from '@/layout/components/ParentView.vue'

// 动态导入所有视图（相对路径，键形如 ../views/dashboard/index.vue）
const modules = import.meta.glob('../views/**/*.vue')

function loadView(component) {
  if (!component) return undefined
  if (component === 'Layout') return Layout
  if (component === 'ParentView') return ParentView
  const key = `../views/${component}.vue`
  return modules[key] || (() => import('../views/error/404.vue'))
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [], // 动态路由（已转换为 vue-router 结构）
    sidebarRoutes: [] // 侧边栏菜单（原始后端结构）
  }),
  actions: {
    generateRoutes(routers) {
      this.sidebarRoutes = routers
      this.routes = this.filterAsyncRoutes(routers, true)
      return this.routes
    },
    filterAsyncRoutes(routers, top) {
      const res = []
      routers.forEach((r) => {
        const meta = {
          title: r.meta && r.meta.title,
          icon: r.meta && r.meta.icon,
          perms: r.meta && r.meta.perms,
          keepAlive: r.keepAlive !== false
        }
        if (r.children && r.children.length) {
          const parent = {
            path: r.path,
            name: r.name,
            component: Layout,
            redirect: r.redirect,
            meta,
            children: this.filterAsyncRoutes(r.children, false)
          }
          res.push(parent)
        } else {
          const view = loadView(r.component)
          if (top) {
            // 顶层叶子：包一层 Layout
            res.push({
              path: '/',
              component: Layout,
              children: [
                {
                  path: (r.path || '').replace(/^\//, ''),
                  name: r.name,
                  component: view,
                  meta
                }
              ]
            })
          } else {
            res.push({ path: r.path, name: r.name, component: view, meta })
          }
        }
      })
      return res
    },
    reset() {
      this.routes = []
      this.sidebarRoutes = []
    }
  }
})
