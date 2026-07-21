<template>
  <div class="pagination" v-if="total > 0">
    <el-pagination
      background
      :current-page="page"
      :page-size="limit"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @update:current-page="(v) => $emit('update:page', v)"
      @update:page-size="(v) => $emit('update:limit', v)"
      @current-change="$emit('load')"
      @size-change="$emit('load')"
    />
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  limit: { type: Number, default: 10 }
})
defineEmits(['update:page', 'update:limit', 'load'])
</script>

<style scoped>
.pagination { display: flex; justify-content: flex-end; padding: 12px 0 0; }
</style>
