import { defineStore } from 'pinia'

const TOKEN_KEY = 'OA_M_TOKEN'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userId: '',
    nickname: '',
    avatar: '',
    roles: []
  }),
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    setInfo(data) {
      this.userId = data.userId
      this.nickname = data.nickname
      this.avatar = data.avatar
      this.roles = data.roles || []
    },
    reset() {
      this.token = ''
      this.userId = ''
      this.nickname = ''
      this.roles = []
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
