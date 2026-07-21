import request from '@/utils/request'

/* ---------- 用户 ---------- */
export function pageUser(params) {
  return request({ url: '/system/user/page', method: 'get', params })
}
export function getUser(id) {
  return request({ url: '/system/user/' + id, method: 'get' })
}
export function getUserRoles(id) {
  return request({ url: '/system/user/' + id + '/roles', method: 'get' })
}
export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}
export function deleteUser(id) {
  return request({ url: '/system/user/' + id, method: 'delete' })
}
export function resetUserPwd(id, password) {
  return request({ url: `/system/user/${id}/reset-pwd`, method: 'put', params: { password } })
}
export function changeUserStatus(id, status) {
  return request({ url: `/system/user/${id}/status`, method: 'put', params: { status } })
}
export function getProfile() {
  return request({ url: '/system/user/profile', method: 'get' })
}
export function updateProfile(data) {
  return request({ url: '/system/user/profile', method: 'put', data })
}
export function updatePassword(oldPassword, newPassword) {
  return request({ url: '/system/user/profile/password', method: 'put', params: { oldPassword, newPassword } })
}

/* ---------- 角色 ---------- */
export function pageRole(params) {
  return request({ url: '/system/role/page', method: 'get', params })
}
export function roleOptions() {
  return request({ url: '/system/role/option', method: 'get' })
}
export function getRole(id) {
  return request({ url: '/system/role/' + id, method: 'get' })
}
export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}
export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}
export function deleteRole(id) {
  return request({ url: '/system/role/' + id, method: 'delete' })
}

/* ---------- 菜单 ---------- */
export function listMenu(params) {
  return request({ url: '/system/menu/list', method: 'get', params })
}
export function getMenu(id) {
  return request({ url: '/system/menu/' + id, method: 'get' })
}
export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}
export function updateMenu(data) {
  return request({ url: '/system/menu', method: 'put', data })
}
export function deleteMenu(id) {
  return request({ url: '/system/menu/' + id, method: 'delete' })
}

/* ---------- 部门 ---------- */
export function deptTree(params) {
  return request({ url: '/system/dept/tree', method: 'get', params })
}
export function getDept(id) {
  return request({ url: '/system/dept/' + id, method: 'get' })
}
export function addDept(data) {
  return request({ url: '/system/dept', method: 'post', data })
}
export function updateDept(data) {
  return request({ url: '/system/dept', method: 'put', data })
}
export function deleteDept(id) {
  return request({ url: '/system/dept/' + id, method: 'delete' })
}

/* ---------- 字典 ---------- */
export function pageDictType(params) {
  return request({ url: '/system/dict/type/page', method: 'get', params })
}
export function dictTypeOptions() {
  return request({ url: '/system/dict/type/option', method: 'get' })
}
export function addDictType(data) {
  return request({ url: '/system/dict/type', method: 'post', data })
}
export function updateDictType(data) {
  return request({ url: '/system/dict/type', method: 'put', data })
}
export function deleteDictType(id) {
  return request({ url: '/system/dict/type/' + id, method: 'delete' })
}
export function dictData(dictType) {
  return request({ url: '/system/dict/data/' + dictType, method: 'get' })
}
export function addDictData(data) {
  return request({ url: '/system/dict/data', method: 'post', data })
}
export function updateDictData(data) {
  return request({ url: '/system/dict/data', method: 'put', data })
}
export function deleteDictData(id) {
  return request({ url: '/system/dict/data/' + id, method: 'delete' })
}

/* ---------- 日志 ---------- */
export function pageLog(params) {
  return request({ url: '/system/log/page', method: 'get', params })
}
export function clearLog() {
  return request({ url: '/system/log/clear', method: 'delete' })
}
export function deleteLog(id) {
  return request({ url: '/system/log/' + id, method: 'delete' })
}
