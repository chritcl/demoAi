<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入角色名..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initRoleList">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="reset">重置</el-button>
      <el-button type="success" :icon="DocumentAdd" @click="handleDialogValue()">新增</el-button>
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
      <el-table-column prop="name" label="角色名" width="100" align="center"/>
      <el-table-column prop="code" label="权限字符" width="200" align="center"/>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
      <el-table-column prop="remark" label="备注"/>
      <el-table-column prop="action" label="操作" width="400" fixed="right" align="center">
        <template v-slot="scope">
          <el-button type="primary" :icon="Tools"  @click="handleMenuDialogValue(scope.row.id)">分配权限</el-button>
          <el-button  type="primary" :icon="Edit" v-if="scope.row.code!='admin'" @click="handleDialogValue(scope.row.id)">编辑</el-button>
          <el-popconfirm v-if="scope.row.code!='admin'" title="您确定要删除这条记录吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button type="danger" :icon="Delete">删除</el-button>
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
  <Dialog :id="id" :dialog-visible="dialogVisible" :dialog-title="dialogTitle" v-model="dialogVisible" @initRoleList="initRoleList"/>
  <MenuDialog v-model="menuDialogVisible" :menuDialogVisible="menuDialogVisible" :id="id" @initRoleList="initRoleList"/>
</template>

<script setup>
import { Search, Refresh, RefreshRight, DocumentAdd, Edit, Delete, Tools } from "@element-plus/icons-vue"
import {ref} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import Dialog from "./components/dialog"
import { ElMessage } from 'element-plus'
import MenuDialog from "./components/menuDialog"

const tableData = ref([])

const total = ref(0)

const queryForm = ref({
  query: '',
  pageNum: 1,
  pageSize: 10
})

/**
 * 菜单分配
 * */
const menuDialogVisible=ref(false)
const handleMenuDialogValue=(userId)=>{
  id.value=userId;
  menuDialogVisible.value=true
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
const initRoleList = async () => {
  const res = await requestUtil.post("sys/role/list", queryForm.value)
  loading_table.value = true
  setTimeout(() => {
    tableData.value = res.data.roleList
    total.value = res.data.total
    loading_table.value = false
  }, 1000)
}

initRoleList()

/**
 * 重置
 * */
const reset = () => {
  queryForm.value.query = ""
  initRoleList()
}

/**
 * 跳转页面
 * */
const handleSizeChange = (pageSize) => {
  queryForm.value.pageNum = 1
  queryForm.value.pageSize = pageSize
  initRoleList()
}

const handleCurrentChange = (pageNum) => {
  queryForm.value.pageNum = pageNum
  initRoleList()
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
  const res=await requestUtil.post("sys/role/delete",ids)
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initRoleList();
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
const id=ref(-1)

/**
 * 添加或者修改显示对话框
 * */
const handleDialogValue=(userId)=>{
  if(userId){
    id.value=userId;
    dialogTitle.value="角色修改"
  }else{
    id.value=-1;
    dialogTitle.value="角色添加"
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
