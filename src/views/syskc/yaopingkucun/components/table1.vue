<template>
  <div ref="dataChart" style="height:100%;width:95%;margin: auto"></div>
</template>

<script setup>
import requestUtil from "@/utils/request"
import {ElMessage} from "element-plus"
import {nextTick, onMounted, ref, toRef} from "vue"

import * as echarts from 'echarts';

const dataChart = ref()

const tabledata=ref([])
const medicine_name=ref([])
const kucun=ref([])
const pay_total=ref([])

const init_table1_List = async () => {
  const res = await requestUtil.post("sys/kc/table1")
  if(res.data.code==200){
    const data=res.data.yaopingkucun
    if(JSON.stringify(tabledata.value) !== JSON.stringify(data)){
      tabledata.value=res.data.yaopingkucun
      medicine_name.value = data.map(item => item.yaopinmingcheng)
      kucun.value = data.map(item => item.shuliang)
      pay_total.value = data.map(item => item.paytotal)
      const myChart = echarts.init(dataChart.value)
      myChart.setOption({
        xAxis: [
          {
            type: 'category',
            // data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            data: medicine_name.value,
            axisPointer: {
              type: 'shadow'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '库存',
            // min: 0,
            // max: 250,
            // interval: 50,
            axisLabel: {
              formatter: '{value} 件'
            }
          },
          {
            type: 'value',
            name: '销售量',
            // min: 0,
            // max: 25,
            // interval: 5,
            axisLabel: {
              formatter: '{value} 件'
            }
          }
        ],
        series: [
          {
            data: kucun.value
          },
          {
            data: pay_total.value
          }
        ]
      })
    }
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
  }
}

init_table1_List()

/**
 * echart
 * */

const initChart=()=>{

  const myChart = echarts.init(dataChart.value);
  let option;

  option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['库存', '销售量']
    },
    xAxis: [
      {
        type: 'category',
        // data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        data: medicine_name.value,
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '库存',
        // min: 0,
        // max: 250,
        // interval: 50,
        axisLabel: {
          formatter: '{value} 件'
        }
      },
      {
        type: 'value',
        name: '销售量',
        // min: 0,
        // max: 25,
        // interval: 5,
        axisLabel: {
          formatter: '{value} 件'
        }
      }
    ],
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: 35,
        moveHandleSize: 1
      },
      {
        type: 'slider',
        show: true,
        yAxisIndex: [0],
        left: '97%',
        start: 0,
        end: 36,
        moveHandleSize: 1
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: 35,
        moveHandleSize: 1
      },
      {
        type: 'inside',
        yAxisIndex: [0],
        start: 0,
        end: 36,
        moveHandleSize: 1
      }
    ],
    series: [
      {
        name: '库存',
        type: 'bar',
        tooltip: {
          valueFormatter: function (value) {
            return value + ' 件';
          }
        },
        itemStyle: {
          borderRadius: [15, 15, 0, 0]
        },
        // data: [
        //   2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3
        // ]
        data: kucun.value
      },
      {
        name: '销售量',
        type: 'line',
        yAxisIndex: 1,
        tooltip: {
          valueFormatter: function (value) {
            return value + ' 件';
          }
        },
        // data: [2.0, 2.2, 3.3, 4.5, 6.3, 10.2, 20.3, 23.4, 23.0, 16.5, 12.0, 6.2]
        data: pay_total.value
      }
    ]
  };

  myChart.setOption(option);
}

setTimeout(() => {
  initChart()
}, 1000)

setInterval(()=>{
  init_table1_List()
},3000)

</script>

<style scoped lang="scss">

</style>
