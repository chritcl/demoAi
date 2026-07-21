import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getInfo } from '@/api/auth'
import { usePermissionStore } from './permission'

const TOKEN_KEY = 'OA_TOKEN'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userId: '',
    username: '',
    nickname: '',
    avatar: '',
    deptName: '',
    roles: [],
    permissions: []
  }),
  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      localStorage.setItem(TOKEN_KEY, this.token)
      return res
    },
    async fetchInfo() {
      const res = await getInfo()
      const data = res.data
      this.userId = data.userId
      this.username = data.username
      this.nickname = data.nickname
      this.avatar = data.avatar
      this.deptName = data.deptName
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      return data
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // ignore
      }
      this.reset()
    },
    reset() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.nickname = ''
      this.roles = []
      this.permissions = []
      localStorage.removeItem(TOKEN_KEY)
      const permStore = usePermissionStore()
      permStore.reset()
    },
    hasPerm(perm) {
      if (!perm) return true
      if (this.permissions.includes('*:*:*')) return true
      return this.permissions.includes(perm)
    }
  }
})
