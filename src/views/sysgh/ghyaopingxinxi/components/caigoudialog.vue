<template>
  <el-dialog model-value="dialogCaigouVisible" title="采购" width="20%" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="采购数量" prop="caigoushuliang" >
        <el-input-number v-model="form.caigoushuliang" :min="1" :max="limitnum" />
      </el-form-item>
      <el-form-item label="采购备注" prop="caigoubeizhu">
        <el-input v-model="form.caigoubeizhu" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="handleConfirm">确认</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {defineEmits, defineProps, ref, watch} from "vue";
import requestUtil from "@/utils/request"
import { ElMessage } from 'element-plus'

const props=defineProps({
  id: {
    type: Number,
    default: -1,
    required: true
  },
  userid: {
    type: Number,
    default: -1,
    required: true
  },
  ghsid:{
    type: Number,
    default: -1,
    required: true
  },
  dialogCaigouVisible: {
    type: Boolean,
    default: false,
    required: true
  }
})

const form=ref({
  id:-1,
  yaopingid:"",
  ygid:"",
  ghsid:"",
  caigoushuliang:"",
  caigoubeizhu:""
})

const emits=defineEmits(['update:modelValue','initMedicineList'])

const formRef=ref(null)

/**
 * 关闭dialog
 * */
const handleClose=()=>{
  emits('update:modelValue',false)
}

/**
 * 规则验证
 * */
const rules=ref({
  caigoushuliang:[
    { required: true, message: '请输入采购数量'}
  ],
  caigoubeizhu:[
    { required: true, message: '请输入采购备注' }
  ]
})

watch(
    ()=>props.dialogCaigouVisible,
    ()=>{
      let id=props.id
      initFormData(props.id)
      if(id!=-1){
        form.value={
          id:-1,
          yaopingid:props.id,
          ygid:props.userid,
          ghsid:props.ghsid,
          caigoushuliang:"",
          caigoubeizhu:" "
        }
      }else{
        form.value={
          id:-1,
          yaopingid:"",
          ygid:"",
          ghsid:"",
          caigoushuliang:"",
          caigoubeizhu:" "
        }
      }
    }
)

const limitnum=ref(10)

/**
 * 根据id查询对应信息
 * */
const initFormData=async(id)=>{
  const res=await requestUtil.get("sys/ghmedicine/"+id)
  limitnum.value=res.data.sysYaopinxinxi.shuliang
}

/**
 * 提交
 * */
const handleConfirm=()=>{
  formRef.value.validate(async(valid)=>{
    if(valid){
      let result=await requestUtil.post("sys/ghmedicine/addghdd/"+form.value.yaopingid+"/"+form.value.ghsid+"/"+form.value.ygid+"/"+form.value.caigoushuliang+"/"+form.value.caigoubeizhu)
      let data=result.data
      if(data.code==200){
        ElMessage.success("执行成功！")
        formRef.value.resetFields()
        emits("initMedicineList")
        handleClose()
      }else{
        ElMessage.error(data.msg)
      }
    }else{
      console.log("fail")
    }
  })
}

</script>

<style scoped lang="scss">

</style>
