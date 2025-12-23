<template>
  <div class="container">
    <dv-loading v-if="loading">加载中，请稍后...</dv-loading>
    <dv-border-box11 title="药店库房管理系统" style="margin: 1.5rem;height: 95%;width: 100%;overflow: hidden" v-if="!loading">
      <div style="margin-top: 60px;margin-left: 15px;margin-right: 5px;">
        <div class="module-box">
          <div style="flex:0 1 30%">
            <!-- 放大的比例默认为0 缩小的比例默认为1  25%是占得百分比-->
            <dv-border-box-1 style="width: 96%;height: 180px;">
              <!-- 边框 -->

            </dv-border-box-1>
            <dv-border-box-8 style="width: 96%;height: 180px;">
              <!-- 边框 -->
            </dv-border-box-8>
            <dv-border-box-8 style="width: 96%;height: 180px;">
              <!-- 边框 -->
            </dv-border-box-8>
          </div>
          <!--                    <div style="flex:0 1 50%">-->
          <!--                      <dv-border-box-11 title="" style="width: 100%;height: 600px;">-->
          <!--                        &lt;!&ndash; 边框 &ndash;&gt;-->
          <!--                      </dv-border-box-11>-->
          <!--                    </div>-->
          <div style="flex:0 1 30%">
            <dv-border-box-8 style="width: 97%;height: 270px;">
              <Table1/>

            </dv-border-box-8>
            <dv-border-box-8 style="width: 97%;height: 270px;">
              <!-- 边框 -->
            </dv-border-box-8>
          </div>
        </div>

        <div class="module-box">
          <div style="flex:0 1 30%">
            <!-- 放大的比例默认为0 缩小的比例默认为1  25%是占得百分比-->
            <dv-border-box-8 style="width: 96%;height: 180px;">
              <!-- 边框 -->
            </dv-border-box-8>
          </div>
          <div style="flex:0 1 40%">
            <dv-border-box-8 style="width: 98%;height: 180px;">
              <!-- 边框 -->
            </dv-border-box-8>
          </div>
          <div style="flex:0 0.9 30%">
            <dv-border-box-8 style="width: 97%;height: 180px;">
              <!-- 边框 -->
            </dv-border-box-8>
          </div>
        </div>
      </div>
    </dv-border-box11>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import Table1 from "./components/table1"

const loading=ref(true)

const initdata = () => {
  setTimeout(() => {
    // total.value = res.data.total
    loading.value = false
  }, 1000)
}

initdata()

</script>

<style scoped>
.container {
  margin: -20px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 104%;
  width: 102%;
  background: black;
}

/* 模块盒子 */
.module-box {
  display: flex;
  /* flex弹性布局 */
  justify-content: space-between; /*两端对齐*/
  margin: 10px;
  /* margin外边距 */
}
</style>
