import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import router from '@/router'
import { getUserMenuPermissionList } from '@/api/modules/common/common'
import { ElMessage } from 'element-plus'
import type { PermissionItemType } from '@/api/modules/settings/permission'
import { createPersistWithExpire } from './persistWithExpire'

const viewModules = import.meta.glob('/src/views/**/*.vue')

export interface MenuItem {
  name: string
  path: string
  title: string
  icon?: string
  order?: number
  children?: MenuItem[]
  model?: number
  otherpath?: string
}

export interface MenuGroup {
  key: string
  title: string
  icon: string
  order: number
  modal: number
  otherpath: string
}

export const useMenuStore = defineStore('menu', {
  state: () => ({
    // 顶级菜单组
    topMenus: [] as MenuGroup[],
    // 侧边菜单（按顶级菜单分组）
    sideMenus: {} as Record<string, MenuItem[]>,
    // 当前选中的顶级菜单
    currentTopMenu: '',
    // 侧边菜单是否折叠
    isMenuCollapsed: false,
    menus: [] as RouteRecordRaw[],
    routesAdded: false,
  }),
  persist: {
    key: 'czzn-menu-store',
    // 过期时间：1 天（1000 * 60 * 60 * 24）
    storage: createPersistWithExpire(localStorage, 1000 * 60 * 60 * 24),
  },
  actions: {
    async loadMenus() {
      // 1. 调接口
      await getUserMenuPermissionList().then((res) => {
        if (res.code === '000') {
          if (res.data.data.length > 0) {
            this.apiDataToRoutes(res.data.data)
            for (const r of this.menus) {
              if (!r || !r.name) {
                console.warn('路由对象缺少 name，跳过：', r)
                continue
              }
              if (!router.hasRoute(r.name)) {
                router.addRoute('dashboard', r)
              } else {
              }
            }
            this.routesAdded = true
            return true
          } else {
            ElMessage.error('未获取菜单权限信息')
            return false
          }
        } else {
          ElMessage.error(res.message)
        }
      })
    },

    //api数据转换为路由结构
    apiDataToRoutes(apiData: PermissionItemType[]) {
      if (apiData.length > 0) {
        apiData.forEach((menu) => {
          this.topMenus.push({
            key: menu.code,
            title: menu.name,
            icon: menu.icon || '',
            order: menu.sortid || 999,
            modal: menu.modal!,
            otherpath: menu.otherpath!,
          })
          // 2. 转换为路由结构
          menu.children?.map((child) => {
            this.generateRoutes(child, menu.code, menu.code)
          })
        })
      }
    },

    //拼接循环函数
    generateRoutes(menu: PermissionItemType, dataCode: string, rootCode: string) {
      if (menu.enable === 1) {
        // 目录
        if (menu.type === 1) {
          if (menu.children && menu.children.length > 0) {
            menu.children?.forEach((child) => {
              this.generateRoutes(child, menu.code, rootCode)
            })
          }
          if (dataCode === rootCode) {
            this.menus.push({
              path: menu.path || '',
              name: menu.code,
              component: () => import('@/views/notblank.vue'),
              meta: {
                title: menu.name,
                icon: menu.icon,
                requiresAuth: true,
                menuGroup: rootCode,
                order: menu.sortid,
                isSubmenu: true,
              },
            })
          } else {
            this.menus.push({
              path: menu.path || '',
              name: menu.code,
              component: () => import('@/views/notblank.vue'),
              meta: {
                title: menu.name,
                icon: menu.icon,
                requiresAuth: true,
                menuGroup: rootCode,
                parentName: dataCode,
                order: menu.sortid,
                isSubmenu: true,
              },
            })
          }
        }
        // 菜单
        else if (menu.type === 2 && menu.path !== '') {
          if (menu.modal === 1) {
            if (dataCode === rootCode) {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                component: viewModules[`/src/views/${menu.component}`],
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  order: menu.sortid,
                },
              })
            } else {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                // component: () => import(menu.component!),
                component: viewModules[`/src/views/${menu.component}`],
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  parentName: dataCode,
                  order: menu.sortid,
                },
              })
            }
          }
          //内链
          else if (menu.modal === 2) {
            if (dataCode === rootCode) {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                component: viewModules[`/src/views/dataV/index.vue`],
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  order: menu.sortid,
                  otherPath: menu.otherpath,
                },
              })
            } else {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                component: viewModules[`/src/views/dataV/index.vue`],
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  parentName: dataCode,
                  order: menu.sortid,
                  otherPath: menu.otherpath,
                },
              })
            }
          } else if (menu.modal === 3 || menu.modal === 4) {
            if (dataCode === rootCode) {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                component: () => import('@/views/notblank.vue'),
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  order: menu.sortid,
                  model: menu.modal,
                  otherpath: menu.otherpath!,
                },
              })
            } else {
              this.menus.push({
                path: menu.path || '',
                name: menu.code,
                component: () => import('@/views/notblank.vue'),
                meta: {
                  title: menu.name,
                  icon: menu.icon,
                  requiresAuth: true,
                  menuGroup: rootCode,
                  parentName: dataCode,
                  order: menu.sortid,
                  model: menu.modal,
                  otherpath: menu.otherpath!,
                },
              })
            }
          }
        }
      }
    },

    // 初始化菜单，从路由生成菜单结构
    initMenuFromRoutes() {
      // this.topMenus.push()

      // 按菜单组分类路由
      const routesByGroup: Record<string, RouteRecordRaw[]> = {}

      // 初始化每个组的路由数组
      this.topMenus.forEach((group) => {
        routesByGroup[group.key] = []
      })

      // 将路由按menuGroup分组
      this.menus.forEach((route) => {
        const menuGroup = route.meta?.menuGroup as string
        if (menuGroup && routesByGroup[menuGroup]) {
          routesByGroup[menuGroup].push(route)
        }
      })

      // 为每个菜单组生成侧边菜单
      Object.keys(routesByGroup).forEach((groupKey) => {
        const routes = routesByGroup[groupKey]
        this.sideMenus[groupKey] = this.generateMenuItems(routes)
      })
      this.currentTopMenu = this.topMenus[0].key
    },

    // 生成菜单项
    generateMenuItems(routes: RouteRecordRaw[]): MenuItem[] {
      if (!routes.length) return []

      // 复制一份路由数组，避免修改原始数据
      const routesCopy = [...routes]

      // 按order排序
      routesCopy.sort((a, b) => {
        const orderA = (a.meta?.order as number) || 0
        const orderB = (b.meta?.order as number) || 0
        return orderA - orderB
      })

      // 创建路由映射表，方便后续查找
      const routeMap: Record<string, RouteRecordRaw> = {}
      routesCopy.forEach((route) => {
        if (route.name) {
          routeMap[route.name as string] = route
        }
      })

      // 找出顶级菜单项（没有parentName的项）
      const menuItems: MenuItem[] = []
      const childItems: Record<string, MenuItem[]> = {}
      const grandChildItems: Record<string, MenuItem[]> = {}

      // 第一遍：将路由分为顶级、子级和孙级三类
      routesCopy.forEach((route) => {
        const parentName = route.meta?.parentName as string

        // 创建菜单项
        const menuItem: MenuItem = {
          name: route.name as string,
          path: this.getFullPath(route.path),
          title: route.meta?.title as string,
          icon: route.meta?.icon as string,
          order: route.meta?.order as number,
          model: route.meta?.model as number,
          otherpath: route.meta?.otherpath as string,
        }

        // 如果标记为子菜单容器，创建空children数组
        if (route.meta?.isSubmenu) {
          menuItem.children = []
        }

        if (!parentName) {
          // 顶级菜单项
          menuItems.push(menuItem)
        } else {
          // 检查父项是否也有父项（即当前是否为孙级菜单）
          const parentRoute = routeMap[parentName]
          if (parentRoute && parentRoute.meta?.parentName) {
            // 孙级菜单
            if (!grandChildItems[parentName]) {
              grandChildItems[parentName] = []
            }
            grandChildItems[parentName].push(menuItem)
          } else {
            // 子级菜单
            if (!childItems[parentName]) {
              childItems[parentName] = []
            }
            childItems[parentName].push(menuItem)
          }
        }
      })

      // 第二遍：将孙级菜单添加到对应的子级菜单中
      Object.keys(grandChildItems).forEach((parentName) => {
        const items = grandChildItems[parentName]
        items.sort((a, b) => (a.order || 0) - (b.order || 0))

        // 找到对应的子级菜单
        Object.keys(childItems).forEach((key) => {
          const children = childItems[key]
          const parent = children.find((item) => item.name === parentName)
          if (parent) {
            if (!parent.children) {
              parent.children = []
            }
            parent.children.push(...items)
            // 确保子菜单按order排序
            parent.children.sort((a, b) => (a.order || 0) - (b.order || 0))
          }
        })
      })

      // 第三遍：将子级菜单添加到对应的顶级菜单中
      Object.keys(childItems).forEach((parentName) => {
        const items = childItems[parentName]
        items.sort((a, b) => (a.order || 0) - (b.order || 0))

        // 找到对应的顶级菜单
        const parent = menuItems.find((item) => item.name === parentName)
        if (parent) {
          if (!parent.children) {
            parent.children = []
          }
          parent.children.push(...items)
          // 确保子菜单按order排序
          parent.children.sort((a, b) => (a.order || 0) - (b.order || 0))
        }
      })

      return menuItems
    },

    // 获取完整路径（添加/dashboard前缀）
    getFullPath(path: string): string {
      if (path === '') return '/dashboard'
      if (path.startsWith('/')) path = path.substring(1)
      return `/dashboard/${path}`
    },

    // 更新当前顶级菜单
    setCurrentTopMenu(menuName: string) {
      this.currentTopMenu = menuName
    },

    // 根据路径获取适合的顶级菜单
    getTopMenuByPath(path: string): string {
      if (path === '/dashboard' || path === '' || path === '/' || path === '/systemOverview') {
        return this.topMenus[0].key // 默认显示乡村治理管理模块
      }

      // 去掉开头的斜杠和dashboard前缀
      let cleanPath = path.startsWith('/') ? path.substring(1) : path
      if (cleanPath.startsWith('dashboard/')) {
        cleanPath = cleanPath.substring('dashboard/'.length)
      }

      // 尝试匹配每个分组下的路径
      const routesByGroup = Object.keys(this.sideMenus)

      for (const groupKey of routesByGroup) {
        // 查找这个组中是否有匹配的路径
        const found = this.findPathInMenuGroup(this.sideMenus[groupKey], cleanPath)
        if (found) {
          return groupKey
        }
      }

      // 提取路径的第一段作为备选匹配
      const firstSegment = cleanPath.split('/')[0]

      // 根据第一级路径判断所属菜单组
      const groupMappings: Record<string, string> = {}
      this.topMenus.forEach((group) => {
        groupMappings[group.key] = group.key
      })

      const result = groupMappings[firstSegment]
      return result
    },

    // 在菜单组中查找路径
    findPathInMenuGroup(menuItems: MenuItem[], path: string): boolean {
      // 如果路径以/dashboard开头，移除前缀
      let searchPath = path
      if (searchPath.startsWith('/dashboard/')) {
        searchPath = searchPath.substring('/dashboard/'.length)
      }

      // 提取路径的第一段作为模块匹配
      const pathSegments = searchPath.split('/')
      const firstSegment = pathSegments[0]

      // 检查这个菜单组是否匹配路径的第一段
      const matchesModule = (key: string, segment: string) => {
        return (
          // 精确匹配
          key === segment
        )
      }

      // 递归查找菜单项中的匹配项
      const findPathInItems = (items: MenuItem[]): boolean => {
        for (const item of items) {
          // 从菜单项路径中提取模块部分
          const itemPath = item.path.startsWith('/dashboard/')
            ? item.path.substring('/dashboard/'.length)
            : item.path

          // 检查路径是否匹配
          if (path === item.path || searchPath === itemPath) {
            console.log('精确路径匹配!')
            return true
          }

          // 检查子菜单
          if (item.children && item.children.length > 0) {
            if (findPathInItems(item.children)) {
              return true
            }
          }
        }
        return false
      }

      // 查找精确匹配
      if (findPathInItems(menuItems)) {
        return true
      }

      // 如果没有精确匹配，但有模块匹配，也返回true
      // 这确保了至少能显示正确的侧边菜单
      if (
        menuItems.some((item) => {
          const itemPath = item.path.startsWith('/dashboard/')
            ? item.path.substring('/dashboard/'.length)
            : item.path
          const itemSegments = itemPath.split('/')
          return matchesModule(firstSegment, itemSegments[0])
        })
      ) {
        console.log('模块级别匹配!')
        return true
      }

      return false
    },

    // 切换菜单折叠状态
    toggleMenuCollapse() {
      this.isMenuCollapsed = !this.isMenuCollapsed
    },

    // 设置菜单折叠状态
    setMenuCollapsed(collapsed: boolean) {
      this.isMenuCollapsed = collapsed
    },

    // 重置
    reset() {
      this.topMenus = []
      this.sideMenus = {}
      this.currentTopMenu = ''
      this.isMenuCollapsed = false
      this.menus = []
      this.routesAdded = false
    },
  },
})
