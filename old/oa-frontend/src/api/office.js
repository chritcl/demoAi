import request from '@/utils/request'

/* 通用：综合办公审批类 */
function make(base) {
  return {
    page: (params) => request({ url: base + '/page', method: 'get', params }),
    get: (id) => request({ url: base + '/' + id, method: 'get' }),
    add: (data) => request({ url: base, method: 'post', data }),
    update: (data) => request({ url: base, method: 'put', data }),
    submit: (id) => request({ url: `${base}/${id}/submit`, method: 'put' }),
    remove: (id) => request({ url: base + '/' + id, method: 'delete' })
  }
}

export const leave = make('/office/leave')
export const vehicle = make('/office/vehicle')
export const seal = make('/office/seal')
export const trip = make('/office/trip')

/* 资产 */
export const asset = {
  page: (params) => request({ url: '/office/asset/page', method: 'get', params }),
  get: (id) => request({ url: '/office/asset/' + id, method: 'get' }),
  add: (data) => request({ url: '/office/asset', method: 'post', data }),
  update: (data) => request({ url: '/office/asset', method: 'put', data }),
  remove: (id) => request({ url: '/office/asset/' + id, method: 'delete' })
}

/* 办公用品 */
export const supply = {
  page: (params) => request({ url: '/office/supply/page', method: 'get', params }),
  get: (id) => request({ url: '/office/supply/' + id, method: 'get' }),
  add: (data) => request({ url: '/office/supply', method: 'post', data }),
  update: (data) => request({ url: '/office/supply', method: 'put', data }),
  adjust: (id, amount) => request({ url: `/office/supply/${id}/stock`, method: 'put', params: { amount } }),
  remove: (id) => request({ url: '/office/supply/' + id, method: 'delete' })
}

/* 考勤 */
export const attendance = {
  clockIn: () => request({ url: '/office/attendance/clock-in', method: 'post' }),
  clockOut: () => request({ url: '/office/attendance/clock-out', method: 'post' }),
  today: () => request({ url: '/office/attendance/today', method: 'get' }),
  my: (params) => request({ url: '/office/attendance/my', method: 'get', params }),
  page: (params) => request({ url: '/office/attendance/page', method: 'get', params }),
  statistics: () => request({ url: '/office/attendance/statistics', method: 'get' })
}
