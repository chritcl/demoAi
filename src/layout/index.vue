<template>
  <div class="app-wrapper">
    <el-container>
      <el-aside width="auto" class="sidebar-container" >
        <Menu/>
      </el-aside>

      <el-container>

        <el-header>
          <Header/>
        </el-header>

        <el-main>
          <router-view/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import Menu from '@/layout/components/menu'
import Header from '@/layout/components/header/index.vue'

</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.sidebar-container {
  background-color: #434a5d;
  height: 100%;
}

.el-container {
  height: 100%
}

.el-header {
  padding-left: 0px;
  padding-right: 0px;
}

:deep(ul.el-menu) {
  border-right-width: 0px
}
</style>


<!--测试-->
<!--<template>-->
<!--  <el-button type="danger" @click="testHandler">测试接口</el-button>-->
<!--  <el-button type="danger" @click="testlogoutHandler">logoout接口</el-button>-->
<!--</template>-->

<!--<script setup>-->
<!--import requestUtil from '@/utils/request'-->

<!--const testHandler=async ()=>{-->
<!--  let result=await requestUtil.get("test/user/list");-->
<!--}-->

<!--const testlogoutHandler=async ()=>{-->
<!--  let result=await requestUtil.get("logout");-->

<!--}-->

<!--</script>-->

<!--<style scoped>-->

<!--</style>-->
