import request from '@/utils/request'

/* 通知公告 */
export function pageNotice(params) {
  return request({ url: '/portal/notice/page', method: 'get', params })
}
export function publishedNotice(params) {
  return request({ url: '/portal/notice/published', method: 'get', params })
}
export function getNotice(id) {
  return request({ url: '/portal/notice/' + id, method: 'get' })
}
export function addNotice(data) {
  return request({ url: '/portal/notice', method: 'post', data })
}
export function updateNotice(data) {
  return request({ url: '/portal/notice', method: 'put', data })
}
export function publishNotice(id) {
  return request({ url: `/portal/notice/${id}/publish`, method: 'put' })
}
export function withdrawNotice(id) {
  return request({ url: `/portal/notice/${id}/withdraw`, method: 'put' })
}
export function deleteNotice(id) {
  return request({ url: '/portal/notice/' + id, method: 'delete' })
}

/* 信息发布 */
export function pageArticle(params) {
  return request({ url: '/portal/article/page', method: 'get', params })
}
export function publishedArticle(params) {
  return request({ url: '/portal/article/published', method: 'get', params })
}
export function getArticle(id) {
  return request({ url: '/portal/article/' + id, method: 'get' })
}
export function addArticle(data) {
  return request({ url: '/portal/article', method: 'post', data })
}
export function updateArticle(data) {
  return request({ url: '/portal/article', method: 'put', data })
}
export function submitArticle(id) {
  return request({ url: `/portal/article/${id}/submit`, method: 'put' })
}
export function passArticle(id, comment) {
  return request({ url: `/portal/article/${id}/pass`, method: 'put', params: { comment } })
}
export function rejectArticle(id, comment) {
  return request({ url: `/portal/article/${id}/reject`, method: 'put', params: { comment } })
}
export function deleteArticle(id) {
  return request({ url: '/portal/article/' + id, method: 'delete' })
}

/* 站内消息 */
export function pageMessage(params) {
  return request({ url: '/portal/message/page', method: 'get', params })
}
export function unreadCount() {
  return request({ url: '/portal/message/unread-count', method: 'get' })
}
export function readMessage(id) {
  return request({ url: `/portal/message/${id}/read`, method: 'put' })
}
export function readAllMessage() {
  return request({ url: '/portal/message/read-all', method: 'put' })
}

/* 门户/工作台 */
export function dashboardSummary() {
  return request({ url: '/portal/dashboard/summary', method: 'get' })
}
export function workbenchStats() {
  return request({ url: '/portal/dashboard/workbench', method: 'get' })
}

/* 文件 */
export function uploadUrl() {
  return (import.meta.env.VITE_APP_BASE_API || '/dev-api') + '/file/upload'
}
export function fileUrl(id) {
  return (import.meta.env.VITE_APP_BASE_API || '/dev-api') + '/file/' + id
}
