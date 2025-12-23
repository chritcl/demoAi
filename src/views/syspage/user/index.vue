<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入用户名..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initUserList">搜索</el-button>
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
      <el-table-column prop="avatar" label="头像" width="80" align="center">
        <template v-slot="scope">
<!--          <img :src="getServerUrl()+'image/userAvatar/'+scope.row.avatar" width="50" height="50"/>-->
          <div style="display: flex; align-items: center">
            <el-image :src="getServerUrl()+'image/userAvatar/'+scope.row.avatar" :preview-src-list="[getServerUrl()+'image/userAvatar/'+scope.row.avatar]" preview-teleported/>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="100" align="center"/>
      <el-table-column prop="roles" label="拥有角色" width="200" align="center">
        <template v-slot="scope">
          <el-tag size="small" type="primary" v-for="item in scope.row.sysRoleList"> {{ item.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="200" align="center"/>
      <el-table-column prop="phonenumber" label="手机号" width="120" align="center"/>
      <el-table-column prop="status" label="状态？" width="200" align="center">
        <template v-slot="{row}">
          <el-switch v-model="row.status" @change="statusChangeHandle(row)" active-text="正常"
                     inactive-text="禁用" active-value="0" inactive-value="1" :disabled="row.username=='admin'"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
      <el-table-column prop="loginDate" label="最后登录时间" width="180" align="center"/>
      <el-table-column prop="remark" label="备注"/>
      <el-table-column prop="action" label="操作" width="450" fixed="right" align="center">
        <template v-slot="scope">
          <el-button type="primary" :icon="Tools" @click="handleRoleDialogValue(scope.row.id,scope.row.sysRoleList)">分配角色</el-button>
          <el-popconfirm v-if="scope.row.username!='admin'" title="您确定要对这个用户重置密码吗？"
                         @confirm="handleResetPassword(scope.row.id)">
            <template #reference>
              <el-button type="warning" :icon="RefreshRight">重置密码</el-button>
            </template>
          </el-popconfirm>

          <el-button  type="primary" :icon="Edit" v-if="scope.row.username!='admin'" @click="handleDialogValue(scope.row.id)">编辑</el-button>

          <el-popconfirm v-if="scope.row.username!='admin'" title="您确定要删除这条记录吗？" @confirm="handleDelete(scope.row.id)">
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
  <Dialog :id="id" :dialog-visible="dialogVisible" :dialog-title="dialogTitle" v-model="dialogVisible" @initUserList="initUserList"/>
  <RoleDialog v-model="roleDialogVisible" :sysRoleList="sysRoleList" :roleDialogVisible="roleDialogVisible" :id="id" @initUserList="initUserList"/>
</template>

<script setup>
import { Search, Refresh, RefreshRight, DocumentAdd, Edit, Delete, Tools } from "@element-plus/icons-vue"
import {ref} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import Dialog from "./components/dialog"
import { ElMessage } from 'element-plus'
import RoleDialog from "./components/roleDialog"

const tableData = ref([])

const total = ref(0)

const queryForm = ref({
  query: '',
  pageNum: 1,
  pageSize: 10
})

/**
 * 角色分配
 * */
const roleDialogVisible=ref(false)
const sysRoleList=ref([])
const handleRoleDialogValue=(userId,roleList)=>{
  id.value=userId;
  sysRoleList.value=roleList;
  roleDialogVisible.value=true
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
const initUserList = async () => {
  const res = await requestUtil.post("sys/user/list", queryForm.value)
  loading_table.value = true
  setTimeout(() => {
    tableData.value = res.data.userList
    total.value = res.data.total
    loading_table.value = false
  }, 1000)
}

initUserList()

/**
 * 重置
 * */
const reset = () => {
  queryForm.value.query = ""
  initUserList()
}

/**
 * 跳转页面
 * */
const handleSizeChange = (pageSize) => {
  queryForm.value.pageNum = 1
  queryForm.value.pageSize = pageSize
  initUserList()
}

const handleCurrentChange = (pageNum) => {
  queryForm.value.pageNum = pageNum
  initUserList()
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
  const res=await requestUtil.post("sys/user/delete",ids)
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initUserList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
  }
}

/**
 * 重置密码
 * */
const handleResetPassword=async (id)=>{
  const res=await requestUtil.get("sys/user/resetPassword/"+id)
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initUserList();
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
  }
}

/**
 * 更改状态
 * */
const statusChangeHandle=async (row)=>{
  let res=await requestUtil.get("sys/user/updateStatus/"+row.id+"/status/"+row.status);
  if(res.data.code==200){
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
  }else{
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initUserList();
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
    dialogTitle.value="用户修改"
  }else{
    id.value=-1;
    dialogTitle.value="用户添加"
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
