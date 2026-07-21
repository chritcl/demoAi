import request from '@/utils/request'

export const login = (data) => request({ url: '/auth/login', method: 'post', data })
export const getInfo = () => request({ url: '/auth/info', method: 'get' })
export const getCaptcha = () => request({ url: '/auth/captcha', method: 'get' })
export const logout = () => request({ url: '/auth/logout', method: 'post' })

export const summary = () => request({ url: '/portal/dashboard/summary', method: 'get' })
export const publishedNotice = (params) => request({ url: '/portal/notice/published', method: 'get', params })
export const getNotice = (id) => request({ url: '/portal/notice/' + id, method: 'get' })
export const publishedArticle = (params) => request({ url: '/portal/article/published', method: 'get', params })
export const pageMessage = (params) => request({ url: '/portal/message/page', method: 'get', params })
export const unreadCount = () => request({ url: '/portal/message/unread-count', method: 'get' })
export const readMessage = (id) => request({ url: `/portal/message/${id}/read`, method: 'put' })

export const todoTasks = () => request({ url: '/flow/task/todo', method: 'get' })
export const doneTasks = () => request({ url: '/flow/task/done', method: 'get' })
export const approveTask = (taskId, comment) => request({ url: '/flow/task/approve', method: 'post', params: { taskId, comment } })
export const rejectTask = (taskId, comment) => request({ url: '/flow/task/reject', method: 'post', params: { taskId, comment } })
export const tasksOfBusiness = (businessType, businessId) => request({ url: '/flow/task/business', method: 'get', params: { businessType, businessId } })

export const contactsTree = () => request({ url: '/contacts/tree', method: 'get' })
export const searchContacts = (keyword) => request({ url: '/contacts/search', method: 'get', params: { keyword } })
