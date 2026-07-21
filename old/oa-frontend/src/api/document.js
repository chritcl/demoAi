import request from '@/utils/request'

export function pageDoc(params) {
  return request({ url: '/document/official/page', method: 'get', params })
}
export function getDoc(id) {
  return request({ url: '/document/official/' + id, method: 'get' })
}
export function addDoc(data) {
  return request({ url: '/document/official', method: 'post', data })
}
export function updateDoc(data) {
  return request({ url: '/document/official', method: 'put', data })
}
export function submitDoc(id) {
  return request({ url: `/document/official/${id}/submit`, method: 'put' })
}
export function deleteDoc(id) {
  return request({ url: '/document/official/' + id, method: 'delete' })
}
export function docStatistics() {
  return request({ url: '/document/official/statistics', method: 'get' })
}
export function genDocNo() {
  return request({ url: '/document/official/gen-doc-no', method: 'get' })
}
