<template>
  <div class="app-container">
    <el-row :gutter="20" class="header">
      <el-col :span="7">
        <el-input placeholder="请输入订单编号..." v-model="queryForm.query" clearable></el-input>
      </el-col>
      <el-button type="primary" :icon="Search" @click="initMedicineList">搜索</el-button>
      <el-button type="warning" :icon="Refresh" @click="reset">重置</el-button>
    </el-row>
    <el-table
        v-loading="loading_table"
        element-loading-text="Loading..."
        :element-loading-spinner="svg"
        element-loading-svg-view-box="-10, -10, 50, 50"
        element-loading-background="rgba(122, 122, 122, 0.3)"
        :data="tableData"
        :span-method="(scope) => objectSpanMethod(scope, tableData)"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange">
      <!--      <el-table-column type="selection" width="55"/>-->
      <el-table-column prop="dingdanbianhao" label="订单编号" width="170" align="center"/>
      <el-table-column prop="yaopingmingchen" label="药品名称" width="170" align="center"/>
      <el-table-column prop="total" label="数量" width="100" align="center"/>
      <el-table-column prop="payMoney" label="总金额" width="100" align="center"/>
      <el-table-column label="订单状态" width="250" align="center">
        <template v-slot="{row}">
          <el-switch v-model="row.status" @change="shenheStatusChangeHandle(row)" active-text="退货"
                     inactive-text="订单完成" active-value="1" inactive-value="0" :disabled="currentUser.roles!=='超级管理员'"
                     style="--el-switch-on-color: #13ce66; --el-switch-off-color: #009dff"/>
        </template>
      </el-table-column>
      <el-table-column prop="yuangongmingchen" label="负责员工" width="100" align="center"/>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
      <el-table-column prop="updateTime" label="更新时间" width="180" align="center"/>
      <el-table-column prop="remark" label="备注" width="100" align="center"/>
      <el-table-column prop="action" label="操作" width="200" fixed="right" align="center">
        <template v-slot="scope">
          <el-button style="margin-bottom: 0.4rem" type="success" :icon="View"
                     @click="handleMedicineDialogValue(scope.row.yaopingid)">药品信息查看
          </el-button>
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
  <MedicineDialog :id="meidcinedialogid" v-model="meidcinedialogVisible" :dialog-visible="meidcinedialogVisible"
                  :dialog-title="meidcinedialogTitle" @initMedicineList="initMedicineList"/>
</template>

<script setup>
import {Search, Refresh, View, Promotion, Edit, Delete, Tools} from "@element-plus/icons-vue"
import {ref} from 'vue'
import requestUtil, {getServerUrl} from "@/utils/request"
import {ElMessage} from 'element-plus'
import MedicineDialog from './components/yaopingdialog'
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
const shenheStatusChangeHandle = async (row) => {
  let res = await requestUtil.get("sys/xsdingdan/updateshenheStatus/" + row.id + "/shenhestatus/" + row.status)
  if (res.data.code === 200) {
    if(row.status==='1'){
      let res1 = await requestUtil.get("sys/xsdingdan/update_tuihuo/" + row.yaopingid + "/shuliang/" + row.total)
      if (res1.data.code === 200){
        ElMessage({
          type: 'success',
          message: '执行成功!'
        })
      }else{
        ElMessage({
          type: 'error',
          message: res.data.msg,
        })
        await initMedicineList()
      }
    } else{
      let res1 = await requestUtil.get("sys/xsdingdan/update_huifu/" + row.yaopingid + "/shuliang/" + row.total)
      if (res1.data.code === 200){
        ElMessage({
          type: 'success',
          message: '执行成功!'
        })
      }else{
        ElMessage({
          type: 'error',
          message: res.data.msg,
        })
        await initMedicineList()
      }
    }

    await initMedicineList()
  } else {
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
    await initMedicineList()
  }
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
  if (currentUser.value.roles !== '超级管理员') {
    const res = await requestUtil.post("sys/xsdingdan/listyg/" + currentUser.value.id, queryForm.value)
    if (res.data.code == 200) {
      loading_table.value = true
      setTimeout(() => {
        tableData.value = res.data.xiaoshoudingdanList
        total.value = res.data.total
        loading_table.value = false
      }, 1000)
    } else {
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
    }
  } else {
    const res = await requestUtil.post("sys/xsdingdan/list", queryForm.value)
    if (res.data.code == 200) {
      loading_table.value = true
      setTimeout(() => {
        tableData.value = res.data.xiaoshoudingdanList
        total.value = res.data.total
        loading_table.value = false
      }, 1000)
    } else {
      ElMessage({
        type: 'error',
        message: res.data.msg,
      })
    }
  }
}

initMedicineList()

const getSpanArr=(data, spanKey)=>{
  var spanArr = []
  var pos = ""
  for (var i = 0; i < data.length; i++) {
    if (i === 0) {
      spanArr.push(1)
      pos = 0
    } else {
      // 判断当前元素与上一个元素是否相同
      if (data[i][spanKey] === data[i - 1][spanKey]) {
        spanArr[pos] += 1
        spanArr.push(0)
      } else {
        spanArr.push(1)
        pos = i
      }
    }
  }
  return spanArr
}

const objectSpanMethod = ({row, column, rowIndex, columnIndex},data) => {
  if (columnIndex < 1) {
  //这里时只有前五列使用合并方法，如果全表格使用，可以删除这个判断
  // data 就是从这里动态传过来的
  // 如果数据中有id，可以传id进行合并，更加准确
  // var spanArr = this.getSpanArr(data, "id");
    var spanArr = getSpanArr(data, column.property)
    const _row = spanArr[rowIndex]
    const _col = _row > 0 ? 1 : 0
    return {
      rowspan: _row,
      colspan: _col,
    };
  } else {
    return
  }
}



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
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
  delBtnStatus.value = selection.length == 0;
}

/**
 * 删除
 * */
const delBtnStatus = ref(true)
const multipleSelection = ref([])
const handleDelete = async (id) => {
  var ids = []
  if (id) {
    ids.push(id)
  } else {
    multipleSelection.value.forEach(row => {
      ids.push(row.id)
    })
  }
  const res = await requestUtil.post("sys/ghdingdang/delete", ids)
  if (res.data.code == 200) {
    ElMessage({
      type: 'success',
      message: '执行成功!'
    })
    await initMedicineList();
  } else {
    ElMessage({
      type: 'error',
      message: res.data.msg,
    })
  }
}

/**
 * 药品信息对话框
 * */
const meidcinedialogVisible = ref(false)
const meidcinedialogTitle = ref('')
const meidcinedialogid = ref(-1)

/**
 * 查看显示药品信息对话框
 * */
const handleMedicineDialogValue = (id) => {
  meidcinedialogid.value = id;
  meidcinedialogTitle.value = "药品信息"
  meidcinedialogVisible.value = true
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
