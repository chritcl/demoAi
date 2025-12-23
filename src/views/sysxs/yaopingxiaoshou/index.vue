<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入药品名称..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initMedicineList">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="reset">重置</el-button>
      <el-badge :value="totalSum" style="margin-left: auto">
        <el-button type="danger" :icon="ShoppingCart" @click="gouwuche_view = true">购物车</el-button>
      </el-badge>
    </el-row>
    <el-table
        v-loading="loading_table"
        element-loading-text="Loading..."
        :element-loading-spinner="svg"
        element-loading-svg-view-box="-10, -10, 50, 50"
        element-loading-background="rgba(122, 122, 122, 0.3)"
        :data="tableData"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column prop="yaopinbianhao" label="药品编号" width="130" align="center"/>
      <el-table-column prop="picture" label="图片" width="130" align="center">
        <template v-slot="scope">
<!--          <img :src="getServerUrl()+'image/medicinepic/'+scope.row.picture" width="120"/>-->
          <div style="display: flex; align-items: center">
            <el-image :src="getServerUrl()+'image/medicinepic/'+scope.row.picture" :preview-src-list="[getServerUrl()+'image/medicinepic/'+scope.row.picture]" preview-teleported/>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="yaopinmingcheng" label="药品名称" width="200" align="center"/>
      <el-table-column prop="yaopinleibie" label="药品类别" width="100" align="center"/>
      <el-table-column prop="guige" label="规格" width="130" align="center"/>
      <el-table-column prop="picihao" label="批次号" width="130" align="center"/>
      <el-table-column prop="shengchanriqi" label="生产日期" width="200" align="center"/>
      <el-table-column prop="shuliang" label="数量" width="100" align="center"/>
      <el-table-column prop="inMoney" label="进价" width="100" align="center"/>
      <el-table-column prop="payMoney" label="售价" width="100" align="center"/>
      <el-table-column prop="gonghuoshanmingchen" label="供货商名称" width="120" align="center"/>
      <el-table-column prop="yuangongmingchen" label="员工名称" width="120" align="center"/>
      <el-table-column prop="remark" label="注意事项" width="150" align="center"/>
      <el-table-column prop="action" label="操作" width="300" fixed="right" align="center">
        <template v-slot="scope">
          <el-button style="margin-bottom: 0.4rem" type="success" :icon="View" @click="handleDialogValue(scope.row.id,'check')">查看</el-button>
          <el-button style="margin-bottom: 0.4rem" type="primary" :icon="Goods" @click="addgood(scope.row)" :disabled="scope.row.shuliang<1||vaildgouwu(scope.row)">加入购物车</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:currentPage="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 30, 40, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
  </div>
  <el-drawer v-model="gouwuche_view">
    <template #header>
      <h2>购物车</h2>
    </template>
    <template #default>
      <el-divider style="margin-top: -1rem"/>
      <div>
        <el-row :gutter="20">
          <el-col
              v-for="index in goods"
              :key="index"
              :span="12"
          >
            <el-card :body-style="{ padding: '0px' }">
              <img
                  :src="getServerUrl()+'image/medicinepic/'+index.picture"
                  class="image"
              />
              <div style="padding: 14px">
                <span>{{ index.yaopinmingcheng }}</span>
                <el-tag type="primary" Small>{{ index.guige }}</el-tag>
                <h2>$ <span style="color:red;font-size: xx-large">{{ index.payMoney }}</span></h2>
                <div class="bottom">
                  <el-input-number v-model="index.total" @change="handleChange(index)" :max="index.shuliang"/>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button type="primary" @click="confirmClick">结算</el-button>
      </div>
    </template>
  </el-drawer>
  <Dialog :id="dialogid" :dialog-visible="dialogVisible" :dialog-title="dialogTitle" v-model="dialogVisible" @initMedicineList="initMedicineList"/>
</template>

<script setup>
import { Search, Refresh, View, Goods, ShoppingCart, Delete, Tools } from "@element-plus/icons-vue"
import {ref, watch} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import Dialog from "./components/dialog"
import {ElMessage, ElMessageBox} from 'element-plus'
import store from "@/store";

const tableData = ref([])

const total = ref(0)

const currentUser = ref(store.getters.GET_USERINFO)

const queryForm = ref({
  query: '',
  pageNum: 1,
  pageSize: 10
})

const loading_table=ref(false)
const svg = `
        <path class="path" d="
          M 30 15
          L 28 17
          M 25.61 25.61
          A 15 15, 0, 0, 1, 15 30
          A 15 15, 0, 1, 1, 27.99 7.5
          L 15 15
        " style="stroke-width: 4px; fill: rgba(0, 0, 0, 0)"/>
      `

/**
 * 初始化&&搜索
 * */
const initMedicineList = async () => {
  const res = await requestUtil.post("sys/xsdingdan/yplist", queryForm.value)
  loading_table.value = true
  setTimeout(() => {
    tableData.value = res.data.yaopinxinxiList
    total.value = res.data.total
    loading_table.value = false
  }, 1000)
}

initMedicineList()

/**
 * 重置
 * */
const reset = () => {
  queryForm.value.query = ""
  initMedicineList()
}

/**
 * 跳转页面
 * */
const handleSizeChange = (pageSize) => {
  queryForm.value.pageNum = 1
  queryForm.value.pageSize = pageSize
  initMedicineList()
}
const handleCurrentChange = (pageNum) => {
  queryForm.value.pageNum = pageNum
  initMedicineList()
}

/**
 * 选中
 * */
const handleSelectionChange=(selection)=>{
  multipleSelection.value=selection;
  delBtnStatus.value=selection.length==0;
}

/**
 * 对话框
 * */
const dialogVisible=ref(false)
const dialogTitle=ref('')
const dialogid=ref(-1)

/**
 * 添加或者修改显示对话框
 * */
const handleDialogValue=(id,text)=>{
  if(!text){
    dialogid.value=id;
    dialogTitle.value="药品信息修改"
  }else{
    dialogid.value=id;
    dialogTitle.value="药品信息查看"
  }

  dialogVisible.value=true
}

/**
 * 购物车
 * */
const gouwuche_view = ref(false)
const goods=ref([])
const clickbutton=ref(false)
const totalSum = ref(0)

const addgood=(row)=>{
  const { id, yaopinbianhao, yaopinmingcheng, picture, guige, payMoney, shuliang } = row
  clickbutton.value=true
  if(goods.value.length>0){
    const foundItem = goods.value.find(item => item.id === row.id);
    if (foundItem) {
      foundItem.total += 1
    } else {
      goods.value.push({ id, yaopinbianhao, yaopinmingcheng, picture, guige, payMoney, shuliang, 'total':1 })
    }
  }else{
    goods.value.push({ id, yaopinbianhao, yaopinmingcheng, picture, guige, payMoney, shuliang, 'total':1 })
  }
}

watch(
    () => clickbutton.value,
    () => {
      totalSum.value = goods.value.reduce((sum, item) => sum + item.total, 0)
      clickbutton.value=false
    }
)

watch(
    () => gouwuche_view.value,
    () => {
      totalSum.value = goods.value.reduce((sum, item) => sum + item.total, 0)
    }
)

const vaildgouwu=(row)=>{
  let status=false
  goods.value.forEach(good => {
    if (good.id===row.id&&good.total === row.shuliang) {
      status=true
    }
  })
  return status
}

const confirmClick=()=> {
  ElMessageBox.confirm(`是否确认要结算？`).then( () => {
    const dataobjs=[]
    goods.value.forEach(good => {
      const dataobj={
        'dingdanbianhao':'',
        'yaopingid':good.id,
        'ygid':currentUser.value.id,
        'total':good.total,
        'payMoney':good.payMoney*good.total,
        'status':0
      }
      dataobjs.push(dataobj)
    })
    requestUtil.post("sys/xsdingdan/settlement",dataobjs).then(result => {
      let data = result.data
      if (data.code == 200) {
        ElMessage.success("执行成功！")
        goods.value=[]
        gouwuche_view.value = false
      } else {
        ElMessage.error(data.msg,'请重新结算订单')
      }
    })
  }).catch(()=> {})

}

const handleChange = (row) => {
  if(row.total===0){
    const indexToDelete = goods.value.findIndex(item => item.id === row.id)
    if (indexToDelete !== -1) {
      goods.value.splice(indexToDelete, 1)
    }
  }
}

</script>

<style scoped lang="scss">
.header {
  padding-bottom: 16px;
  box-sizing: border-box;
}

.el-pagination {
  float: right;
  padding: 20px;
  box-sizing: border-box;
}

::v-deep th.el-table__cell {
  word-break: break-word;
  background-color: #f8f8f9 !important;
  color: #515a6e;
  height: 40px;
  font-size: 13px;
}

.el-tag--small {
  margin-left: 5px;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
  margin-right: 10px;
  align-items: center;
}

.image {
  height: 12rem;
  display: block;
}

.button {
  padding: 0;
  min-height: auto;
}

</style>
