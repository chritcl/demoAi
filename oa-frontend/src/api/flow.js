import request from '@/utils/request'

export function listFlowDefinition() {
  return request({ url: '/flow/definition/list', method: 'get' })
}
export function getFlowDefinition(id) {
  return request({ url: '/flow/definition/' + id, method: 'get' })
}
export function saveFlowDefinition(data) {
  return request({ url: '/flow/definition', method: 'post', data })
}

export function approveTask(taskId, comment) {
  return request({ url: '/flow/task/approve', method: 'post', params: { taskId, comment } })
}
export function rejectTask(taskId, comment) {
  return request({ url: '/flow/task/reject', method: 'post', params: { taskId, comment } })
}
export function transferTask(taskId, toUserId, comment) {
  return request({ url: '/flow/task/transfer', method: 'post', params: { taskId, toUserId, comment } })
}

export function todoTasks() {
  return request({ url: '/flow/task/todo', method: 'get' })
}
export function doneTasks() {
  return request({ url: '/flow/task/done', method: 'get' })
}
export function mineInstances() {
  return request({ url: '/flow/instance/mine', method: 'get' })
}
export function getInstance(id) {
  return request({ url: '/flow/instance/' + id, method: 'get' })
}
export function instanceByBusiness(businessType, businessId) {
  return request({ url: '/flow/instance/by-business', method: 'get', params: { businessType, businessId } })
}
export function tasksOfBusiness(businessType, businessId) {
  return request({ url: '/flow/task/business', method: 'get', params: { businessType, businessId } })
}
