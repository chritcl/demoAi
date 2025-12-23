<template>
  <el-dialog model-value="roleDialogVisible" title="分配权限" width="30%" @close="handleClose">
    <p>请选择供应商名称</p>
      <el-select
          v-model="value"
          filterable
          placeholder="请选择供应商名称"
          style="width: 240px"
      >
        <el-option
            v-for="item in form"
            :key="item.userId"
            :label="item.username"
            :value="item.userId"
        />
      </el-select>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="handleConfirm">确认</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {defineEmits, defineProps, ref, watch} from "vue"
import requestUtil,{getServerUrl} from "@/utils/request"
import { ElMessage } from 'element-plus'

const props=defineProps({
  ghsid:{
    type:Number,
    default:-1,
    required:true
  },
  userid:{
    type:Number,
    default:"",
    required:true
  },
  userDialogVisible:{
    type:Boolean,
    default:false,
    required:true
  }
})

const form=ref([])
const value = ref()

const initFormData=async()=>{
  const res=await requestUtil.post("sys/ghs/grantUser")
  form.value=res.data.sysUserRoleList
  value.value=props.userid
}

watch(
    ()=>props.userDialogVisible,
    ()=>{
      let id=props.ghsid;
      // console.log("id="+id)
      if(id!=-1){
        initFormData()
      }
    }
)

const emits=defineEmits(['update:modelValue','initGhsList'])

/**
 * 关闭dialog
 * */
const handleClose=()=>{
  emits('update:modelValue',false)
}

/**
 * 保存
 * */
const handleConfirm=()=>{
  if(value.value){
    requestUtil.post("sys/ghs/updateGhsUser/"+value.value+"/"+props.ghsid).then(result=>{
      let data=result.data
      if(data.code==200){
        ElMessage.success("执行成功！")
        emits("initGhsList")
        handleClose()
      }else{
        ElMessage.error(data.msg)
      }
    })
  }else{
    ElMessage.error("异常，输入有误，请联系管理员！")
  }
}

</script>


<style scoped lang="scss">

</style>
