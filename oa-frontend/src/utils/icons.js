// 后端菜单 icon 字符串 -> Element Plus 图标组件名映射
const map = {
  dashboard: 'Monitor',
  desktop: 'Monitor',
  read: 'Reading',
  message: 'ChatDotRound',
  documentation: 'Document',
  document: 'Document',
  edit: 'Edit',
  download: 'Download',
  search: 'Search',
  chart: 'DataAnalysis',
  tool: 'Tools',
  date: 'Calendar',
  car: 'Van',
  stamp: 'Stamp',
  plane: 'Plane',
  shopping: 'Goods',
  box: 'Box',
  time: 'Clock',
  people: 'UserFilled',
  guide: 'Guide',
  list: 'List',
  tree: 'Share',
  'tree-table': 'Grid',
  system: 'Setting',
  user: 'User',
  peoples: 'Avatar',
  menu: 'Menu',
  dict: 'Collection',
  log: 'Tickets',
  bell: 'Bell',
  home: 'HomeFilled'
}

export function resolveIconName(name) {
  if (!name) return ''
  return map[name] || map[name.toLowerCase()] || 'Menu'
}
