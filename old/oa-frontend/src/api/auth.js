import request from '@/utils/request'

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

export function getInfo() {
  return request({ url: '/auth/info', method: 'get' })
}

export function getRouters() {
  return request({ url: '/auth/routers', method: 'get' })
}

export function getCaptcha() {
  return request({ url: '/auth/captcha', method: 'get' })
}
