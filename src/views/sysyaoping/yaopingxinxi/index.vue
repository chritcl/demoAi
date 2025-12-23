<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入药品名称..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initMedicineList">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="reset">重置</el-button>
      <el-popconfirm title="您确定批量删除这些记录吗？" @confirm="handleDelete(null)">
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
          <el-button style="margin-bottom: 0.4rem" type="warning" :icon="Tools" @click="handleRoleDialogValue(scope.row.id,scope.row.yuangongid,scope.row.gonghuoshangid)" v-if="currentUser.roles==='超级管理员'">供货商&员工分配</el-button>
          <el-button style="margin-bottom: 0.4rem" type="primary" :icon="Edit" @click="handleDialogValue(scope.row.id)">编辑</el-button>
          <el-button style="margin-bottom: 0.4rem" type="success" :icon="View" @click="handleDialogValue(scope.row.id,'check')">查看</el-button>
          <el-popconfirm v-if="scope.row.username!='admin'" title="您确定要删除这条记录吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button style="margin-bottom: 0.4rem" type="danger" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>

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
  <Dialog :id="dialogid" :dialog-visible="dialogVisible" :dialog-title="dialogTitle" v-model="dialogVisible" @initMedicineList="initMedicineList"/>
  <UserDialog v-model="userDialogVisible" :yuangongid="yuangongid" :gonghuoshangid="gonghuoshangid" :userDialogVisible="userDialogVisible" :id="dialogid" @initMedicineList="initMedicineList"/>
</template>

<script setup>
import { Search, Refresh, View, DocumentAdd, Edit, Delete, Tools } from "@element-plus/icons-vue"
import {ref} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import Dialog from "./components/dialog"
import { ElMessage } from 'element-plus'
import UserDialog from "./components/userDialog"
import store from "@/store";

const tableData = ref([])

const total = ref(0)

const currentUser = ref(store.getters.GET_USERINFO);

const queryForm = ref({
  query: '',
  pageNum: 1,
  pageSize: 10
})

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
  const res = await requestUtil.post("sys/medicine/list", queryForm.value)
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
  const res=await requestUtil.post("sys/medicine/delete",ids)
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
const dialogid=ref(-1)

/**
 * 添加或者修改显示对话框
 * */
const handleDialogValue=(id,text)=>{
  if(!text){
    console.log(id)
    dialogid.value=id;
    dialogTitle.value="药品信息修改"
  }else{
    dialogid.value=id;
    dialogTitle.value="药品信息查看"
  }
  dialogVisible.value=true
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
