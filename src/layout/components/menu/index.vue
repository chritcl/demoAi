<template>
  <el-menu
      active-text-color="#ffd04b"
      background-color="#2d3a4b"
      class="el-menu-vertical-demo"
      text-color="#fff"
      router
      :collapse="isCollapse"
  >
    <el-menu-item @click="sidebar_handler()">
      <h1 v-if="!isCollapse"  style="margin-right: 10px">药店库房管理系统</h1>
<!--      <el-button type="info" :icon="Expand" circle @click="sidebar_handler()"/>-->
      <el-icon class="icon_item" :class="{'icon_item':true,'rotate':isCollapse}">
        <Expand/>
      </el-icon>
    </el-menu-item>
    <el-menu-item index="/index">
      <el-icon>
        <home-filled/>
      </el-icon>
      <span>首页</span>
    </el-menu-item>
    <el-sub-menu :index="menu.path" v-for="menu in menuList">
      <template #title>
        <el-icon>
          <svg-icon :icon="menu.icon"/>
        </el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <el-menu-item :index="item.path" v-for="item in menu.children">
        <el-icon>
          <svg-icon :icon="item.icon"/>
        </el-icon>
        <span>{{ item.name }}</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { HomeFilled, Expand } from '@element-plus/icons-vue'
import {inject, ref} from 'vue'
import store from '@/store'

const isCollapse=ref(false)

const sidebar_handler=()=>{
  isCollapse.value=!isCollapse.value
}

const menuList = ref(store.getters.GET_MENULIST)

</script>

<style scoped lang="scss">
.icon_item{
  transition: transform 2s;
}

.rotate {
  animation: rotate 0.5s forwards;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(180deg);
  }
}
</style>
