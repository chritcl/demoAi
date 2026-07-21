import { ref } from 'vue'
import { dictData } from '@/api/system'

const cache = new Map()

/**
 * 字典数据组合式：自动缓存
 * @param {string} type 字典类型
 */
export function useDict(type) {
  const data = ref(cache.get(type) || [])
  if (!cache.has(type)) {
    dictData(type).then((res) => {
      const list = res.data || []
      cache.set(type, list)
      data.value = list
    })
  }
  return data
}

export function dictLabel(type, value) {
  const list = cache.get(type) || []
  const item = list.find((d) => String(d.dictValue) === String(value))
  return item ? item.dictLabel : value
}

export function dictTag(type, value) {
  const list = cache.get(type) || []
  return (list.find((d) => String(d.dictValue) === String(value)) || {}).listClass || ''
}
