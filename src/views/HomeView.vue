<template>
  <div class="container">
<!--    <div>-->
<!--      <el-row>-->
<!--        <el-col :span="6">-->
<!--&lt;!&ndash;          <el-statistic title="Daily active users" :value="date" />&ndash;&gt;-->
<!--          <time>{{date}}</time>-->
<!--        </el-col>-->
<!--        <el-col :span="6">-->
<!--          <el-statistic :value="138">-->
<!--            <template #title>-->
<!--              <div style="display: inline-flex; align-items: center">-->
<!--                Ratio of men to women-->
<!--                <el-icon style="margin-left: 4px" :size="12">-->
<!--                  <Male />-->
<!--                </el-icon>-->
<!--              </div>-->
<!--            </template>-->
<!--            <template #suffix>/100</template>-->
<!--          </el-statistic>-->
<!--        </el-col>-->
<!--        <el-col :span="6">-->
<!--          <el-statistic title="Total Transactions" :value="outputValue" />-->
<!--        </el-col>-->
<!--        <el-col :span="6">-->
<!--          <el-statistic title="Feedback number" :value="562">-->
<!--            <template #suffix>-->
<!--              <el-icon style="vertical-align: -0.125em">-->
<!--                <ChatLineRound />-->
<!--              </el-icon>-->
<!--            </template>-->
<!--          </el-statistic>-->
<!--        </el-col>-->
<!--      </el-row>-->
<!--    </div>-->
<!--    <el-image :src="photo" style="maigin: 0rem;width: 100%;height: 53rem"/>-->
    <el-image :src="photo" fit="cover" style="width: 100%;height: 110%;"/>
<!--    <Table1/>-->
  </div>
</template>

<script>
// import HelloWorld from '@/components/HelloWorld.vue'


// export default {
//   name: 'HomeView',
//   components: {
//     HelloWorld
//   }
// }
</script>

<script setup>
import { ref } from 'vue'
import { useTransition } from '@vueuse/core'
import { ChatLineRound, Male } from '@element-plus/icons-vue'
import photo from '../assets/homeview2.jpg'
import Table1 from "@/views/syskc/yaopingkucun/components/table1.vue";

const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' };
const date = ref(new Date())

const source = ref(0)
const outputValue = useTransition(source, {
  duration: 1500,
})
source.value = 172000
</script>

<style scoped>
.container {
  margin: -20px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.el-col {
  text-align: center;
}
</style>
