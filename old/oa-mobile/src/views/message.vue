<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="消息中心" left-arrow @click-left="$router.back()" fixed />
    <van-cell-group>
      <van-cell v-for="m in list" :key="m.id" :title="m.title" :label="m.content" is-link @click="read(m)">
        <template #right-icon>
          <van-tag type="danger" v-if="m.isRead === 0" round>未读</van-tag>
        </template>
      </van-cell>
    </van-cell-group>
    <van-empty v-if="!list.length" description="暂无消息" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageMessage, readMessage } from '@/api'

const list = ref([])
async function load() { list.value = ((await pageMessage({ pageNum: 1, pageSize: 50 })).data || {}).list || [] }
async function read(m) { if (m.isRead === 0) { await readMessage(m.id); m.isRead = 1 } }
onMounted(load)
</script>
