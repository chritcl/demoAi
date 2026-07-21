<template>
  <template v-if="!item.meta?.hidden && !item.hidden">
    <!-- 只有一个可见子节点：直接渲染为菜单项 -->
    <el-menu-item v-if="visibleChildren.length === 1 && !alwaysShowChildren" :index="resolvePath(visibleChildren[0])">
      <el-icon v-if="iconOf(visibleChildren[0] || item)"><component :is="iconOfName(visibleChildren[0] || item)" /></el-icon>
      <template #title>{{ titleOf(visibleChildren[0] || item) }}</template>
    </el-menu-item>

    <!-- 无子节点：叶子菜单 -->
    <el-menu-item v-else-if="visibleChildren.length === 0" :index="resolvePath(item)">
      <el-icon v-if="iconOf(item)"><component :is="iconOfName(item)" /></el-icon>
      <template #title>{{ titleOf(item) }}</template>
    </el-menu-item>

    <!-- 多子节点：子菜单 -->
    <el-sub-menu v-else :index="resolvePath(item)">
      <template #title>
        <el-icon v-if="iconOf(item)"><component :is="iconOfName(item)" /></el-icon>
        <span>{{ titleOf(item) }}</span>
      </template>
      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path + (child.name || '')"
        :item="child"
        :base-path="resolvePath(item)"
      />
    </el-sub-menu>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { resolveIconName } from '@/utils/icons'

const props = defineProps({
  item: { type: Object, required: true },
  basePath: { type: String, default: '' }
})

const visibleChildren = computed(() => {
  return (props.item.children || []).filter((c) => !c.hidden && !(c.meta && c.meta.hidden))
})

const alwaysShowChildren = computed(() => props.item.meta && props.item.meta.alwaysShow)

function titleOf(node) {
  return (node.meta && node.meta.title) || node.name || ''
}
function iconOf(node) {
  return resolveIconName((node.meta && node.meta.icon) || '')
}
function iconOfName(node) {
  return resolveIconName((node.meta && node.meta.icon) || '')
}
function resolvePath(node) {
  const p = node.path || ''
  if (p.startsWith('/')) return p
  if (props.basePath) {
    return props.basePath.replace(/\/$/, '') + '/' + p
  }
  return '/' + p
}
</script>
