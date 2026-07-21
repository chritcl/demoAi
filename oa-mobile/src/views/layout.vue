<template>
  <div class="layout">
    <keep-alive>
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </keep-alive>
    <van-tabbar v-model="active" route>
      <van-tabbar-item to="/home" icon="wap-home-o">工作台</van-tabbar-item>
      <van-tabbar-item to="/todo" icon="todo-list-o">待办</van-tabbar-item>
      <van-tabbar-item to="/contacts" icon="contact">通讯录</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const active = ref(0)
</script>

<style scoped>
.layout { min-height: 100vh; }
</style>
