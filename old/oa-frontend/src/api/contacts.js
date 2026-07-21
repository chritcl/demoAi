import request from '@/utils/request'

export function contactsTree() {
  return request({ url: '/contacts/tree', method: 'get' })
}

export function searchContacts(keyword) {
  return request({ url: '/contacts/search', method: 'get', params: { keyword } })
}
