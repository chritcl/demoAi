<template>
  <el-dropdown trigger="click">
    <span class="el-dropdown-link">
      <el-avatar shape="circle" :size="40" :src="squareUrl"/>
      &nbsp;&nbsp;{{ currentUser.username }}，&nbsp;你好！<el-icon class="el-icon--right"><Arrow-down/></el-icon>
    </span>

    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item>
          <router-link :to="{name:'个人中心'}">个人中心</router-link>
        </el-dropdown-item>
        <el-dropdown-item @click="logout" divided>安全退出</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import requestUtil,{ getServerUrl } from '@/utils/request'
import router from "@/router"
import { ArrowDown } from '@element-plus/icons-vue'

const store=useStore()

const currentUser = ref(store.getters.GET_USERINFO);
const squareUrl=ref(getServerUrl()+'image/userAvatar/'+currentUser.value.avatar)

const logout=async ()=>{
  let result=await requestUtil.get("/logout")

  if(result.data.code===200){
    // store.commit("RESET_TABS")
    store.commit("SET_ROUTES_STATE",false)
    await store.dispatch('logout')
  }
}
</script>

<style scoped lang="scss">
.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
a {
  text-decoration: none;
  color: black;
}
.router-link-active{
  color: black;

  text-decoration: none;
}
</style>
