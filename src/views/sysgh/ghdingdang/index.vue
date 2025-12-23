<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入订单编号..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initMedicineList">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="reset">重置</el-button>
      <el-popconfirm title="您确定批量删除这些记录吗？" @confirm="handleDelete(null)" v-if="currentUser.roles==='超级管理员'">
        <template #reference>
          <el-button type="danger" :disabled="delBtnStatus" :icon="Delete">批量删除</el-button>
        </template>
      </el-popconfirm>
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
        :resizable="true"
        @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"/>
      <el-table-column prop="id" label="订单编号" width="170" align="center"/>
      <el-table-column prop="yaopingmingchen" label="药品名称" width="130" align="center"/>
      <el-table-column prop="gonghuoshanmingchen" label="供货商名称" width="100" align="center"/>
      <el-table-column prop="yuangongmingchen" label="负责员工" width="100" align="center"/>
      <el-table-column prop="caigoushuliang" label="采购数量" width="100" align="center"/>
      <el-table-column prop="remark" label="备注" width="150" align="center"/>
      <el-table-column prop="pay" label="支付状态" width="100" align="center">
        <template v-slot="{row}">
<!--          <el-button v-if="row.pay==='未支付'&&currentUser.roles==='供货商'" @click="payHandle(row)">支付</el-button>-->
          <el-popconfirm title="您确定要支付该订单吗？" @confirm="payHandle(row)" v-if="row.pay==='未支付'&&currentUser.roles==='超级管理员'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="row.pay==='未支付'&&currentUser.roles==='超级管理员'">支付</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="120" align="center"/>
      <el-table-column label="审核状态" width="180" align="center">
        <template v-slot="{row}">
          <el-switch v-model="row.shenghezhuangtai" @change="shenheStatusChangeHandle(row)" active-text="审核"
                     inactive-text="未审核" active-value="1" inactive-value="0" :disabled="currentUser.username!='admin'||row.pay!=='已支付'" v-if="row.status==='待发货'||row.status==='待审核'||row.status==='待支付'" />
          <a v-else>已审核</a>
        </template>
      </el-table-column>
      <el-table-column prop="shenghebeizhu" label="审核备注" width="150" align="center"/>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
      <el-table-column prop="updateTime" label="更新时间" width="180" align="center"/>
      <el-table-column prop="action" label="操作" width="300" fixed="right" align="center">
        <template v-slot="scope">
<!--          <el-button style="margin-bottom: 0.4rem" type="warning" :icon="Promotion" @click="fahuoHandle(scope.row)" v-if="currentUser.roles==='供货商'&&scope.row.status==='待发货'">发货</el-button>-->
<!--          <el-button style="margin-bottom: 0.4rem" type="warning" :icon="Tools" @click="handleRoleDialogValue(scope.row.id,scope.row.ygid,scope.row.ghsid)" v-if="currentUser.roles==='超级管理员'" :disabled="scope.row.status==='退货完成订单关闭'">供货商&员工分配</el-button>-->
          <el-button style="margin-bottom: 0.4rem" type="primary" :icon="Edit" @click="handleDialogValue(scope.row.id)" v-if="currentUser.roles!=='供货商'" :disabled="scope.row.status==='退货完成订单关闭'">编辑</el-button>
          <el-button style="margin-bottom: 0.4rem" type="success" :icon="View" @click="handleMedicineDialogValue(scope.row.yaopingid)">药品信息查看</el-button>
          <el-popconfirm title="您确定要发出该订单吗？" @confirm="fahuoHandle(scope.row)" v-if="currentUser.roles==='供货商'&&scope.row.status==='待发货'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="currentUser.roles==='供货商'&&scope.row.status==='待发货'">发货</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm v-if="scope.row.username!='admin'" title="您确定要删除这条记录吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" type="danger" :icon="Delete" v-if="currentUser.roles==='超级管理员'">删除</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="您确定要确认收货吗？" @confirm="shouhuoHandle(scope.row)" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待收货入库'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待收货入库'" type="info">收货</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="您确定要发起退货吗？" @confirm="tuihuoHandle(scope.row)" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待收货入库'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待收货入库'" type="info">退货</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="您确定要确认退货吗？" @confirm="alreadtuihuoHandle(scope.row)" v-if="currentUser.roles==='供货商'&&scope.row.status==='待退货'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="currentUser.roles==='供货商'&&scope.row.status==='待退货'" type="info">确认退货</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="您确定要确认入库吗？" @confirm="alreadshouhuoHandle(scope.row)" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待清点入库'">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" v-if="currentUser.roles!=='供货商'&&scope.row.status==='待清点入库'" type="primary">确认入库</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:currentPage="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[5,10, 20, 30, 40, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
  </div>
  <Dialog :id="dialogid" :dialog-visible="dialogVisible" :dialog-title="dialogTitle" v-model="dialogVisible" @initMedicineList="initMedicineList"/>
  <MedicineDialog :id="meidcinedialogid" v-model="meidcinedialogVisible" :dialog-visible="meidcinedialogVisible" :dialog-title="meidcinedialogTitle" @initMedicineList="initMedicineList"/>
  <UserDialog v-model="userDialogVisible" :yuangongid="yuangongid" :gonghuoshangid="gonghuoshangid" :userDialogVisible="userDialogVisible" :id="dialogid" @initMedicineList="initMedicineList"/>
</template>

<script setup>
import { Search, Refresh, View, Promotion, Edit, Delete, Tools } from "@element-plus/icons-vue"
import {ref} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import { ElMessage } from 'element-plus'
import Dialog from "./components/dialog"
import MedicineDialog from './components/yaopingdialog'
import UserDialog from "./components/userDialog"
import store from "@/store"

const currentUser = ref(store.getters.GET_USERINFO);

const tableData = ref([])

const total = ref(0)

const queryForm = ref({
  query: '',
  pageNum: 1,
  pageSize: 10
})

/**
 * 更改审核状态
 * */
const shenheStatusChangeHandle=async (row)=>{
  let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/shenhestatus/"+row.shenghezhuangtai);
  if(res.data.code==200){
    if(row.shenghezhuangtai=='1'){
      let res1=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"待发货")
      if(res1.data.code==200){
        ElMessage({
          type: 'success',
          message: '执行成功!'
        })
        await initMedicineList();
      }else{
        ElMessage({
          type: 'error',
          message: res.data.msg,
        })
        await initMedicineList();
      }
    }else{
      let res1=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+'待审核')
      if(res1.data.code==200){
        ElMessage({
          type: 'success',
          message: '执行成功!'
        })
        await initMedicineList();
      }else{
        ElMessage({
          type: 'error',
          message: res.data.msg,
        })
        await initMedicineList();
      }
    }
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 支付订单
 * */
const payHandle= async (row)=>{
  const res=await requestUtil.get("sys/ghdingdang/updatePay/"+row.id)
  if(res.data.code==200){
    let res1=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"待审核")
    if(res1.data.code==200){
      ElMessage({
        type: 'success',
        message: '执行成功!'
      })
      await initMedicineList();
    }else{
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
      await initMedicineList();
    }
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 发货
 * */
const fahuoHandle= async (row)=>{
  let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"待收货入库")
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initMedicineList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 收货
 * */
const shouhuoHandle= async (row)=>{
  let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"待清点入库")
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initMedicineList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 退货
 * */
const tuihuoHandle= async (row)=>{
  ElMessage({
    type: 'warning',
    message: '请填写退货理由!'
  })
  let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"待退货")
  if(res.data.code==200){
    await handleDialogValue(row.id)
    await initMedicineList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 确认退货
 * */
const alreadtuihuoHandle= async (row)=>{
  let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"退货完成订单关闭")
  if(res.data.code==200){
    await initMedicineList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList();
  }
}

/**
 * 确认退货
 * */
const alreadshouhuoHandle= async (row)=>{
  let res1=await requestUtil.post("sys/ghdingdang/saveinmedicine/"+row.yaopingid+"/"+row.caigoushuliang)
  if(res1.data.code==200){
    let res=await requestUtil.get("sys/ghdingdang/updateshenheStatus/"+row.id+"/status/"+"订单完成，已入库")
    if(res.data.code==200){
      ElMessage({
        type: 'success',
        message: '执行成功!'
      })
      await initMedicineList();
    }else{
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
      await initMedicineList();
    }
  }else{
    ElMessage({
      type: 'error',
      message: res1.data.msg,
    })
    await initMedicineList();
  }

}

/**
 * 分配供应商和员工
 * */
const userDialogVisible=ref(false)
const yuangongid=ref(-1)
const gonghuoshangid=ref(-1)
const handleRoleDialogValue=(id,ygid,ghsid)=>{
  dialogid.value=id;
  yuangongid.value=ygid;
  gonghuoshangid.value=ghsid;
  userDialogVisible.value=true
}

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
  if(currentUser.value.roles==='供货商'){
    const res = await requestUtil.post("sys/ghdingdang/listgh/"+currentUser.value.id, queryForm.value)
    if(res.data.code==200){
      loading_table.value = true
      setTimeout(() => {
        tableData.value = res.data.caigoudingdanList
        total.value = res.data.total
        loading_table.value = false
      }, 1000)
    }else {
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
    }
  } else if(currentUser.value.roles==='员工'){
    const res = await requestUtil.post("sys/ghdingdang/listyg/"+currentUser.value.id, queryForm.value)
    if(res.data.code==200){
      loading_table.value = true
      setTimeout(() => {
        tableData.value = res.data.caigoudingdanList
        total.value = res.data.total
        loading_table.value = false
      }, 1000)
    }else {
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
    }
  } else {
    const res = await requestUtil.post("sys/ghdingdang/list", queryForm.value)
    if(res.data.code==200){
      loading_table.value = true
      setTimeout(() => {
        tableData.value = res.data.caigoudingdanList
        total.value = res.data.total
        loading_table.value = false
      }, 1000)
    }else {
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
    }
  }
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
 * 删除
 * */
const delBtnStatus=ref(true)
const multipleSelection=ref([])
const handleDelete=async (id)=>{
  var ids = []
  if(id){
    ids.push(id)
  }else{
    multipleSelection.value.forEach(row=>{
      ids.push(row.id)
    })
  }
  const res=await requestUtil.post("sys/ghdingdang/delete",ids)
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initMedicineList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
  }
}

/**
 * 对话框
 * */
const dialogVisible=ref(false)
const dialogTitle=ref('')
const dialogid=ref('-1')

/**
 * 添加或者修改显示对话框
 * */
const handleDialogValue=(id,text)=>{
  if(!text){
    dialogid.value=id;
    dialogTitle.value="订单信息修改"
  }else{
    dialogid.value=id;
    dialogTitle.value="error"
  }
  dialogVisible.value=true
}

/**
 * 药品信息对话框
 * */
const meidcinedialogVisible=ref(false)
const meidcinedialogTitle=ref('')
const meidcinedialogid=ref(-1)

/**
 * 查看显示药品信息对话框
 * */
const handleMedicineDialogValue=(id)=>{
  meidcinedialogid.value=id;
  meidcinedialogTitle.value="药品信息"
  meidcinedialogVisible.value=true
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
</style>
