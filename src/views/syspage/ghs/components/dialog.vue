<template>
  <el-dialog model-value="dialogVisible" :title="dialogTitle" width="30%" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="供货商名称" prop="gonghuoshangmingchen">
        <el-input v-model="form.gonghuoshangmingchen"/>
      </el-form-item>

      <el-form-item label="法人姓名" prop="farenxinming">
        <el-input v-model="form.farenxinming"/>
      </el-form-item>

      <el-form-item label="法人身份证号" prop="farensfz">
        <el-input v-model="form.farensfz"/>
      </el-form-item>

      <el-form-item label="地址" prop="dizhi">
        <el-input v-model="form.dizhi"/>
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
import {defineEmits, defineProps, ref, watch} from "vue"
import requestUtil from "@/utils/request"
import { ElMessage } from 'element-plus'

const props=defineProps({
  ghsid:{
    type:Number,
    default:-1,
    required:true
  },
  dialogTitle:{
    type:String,
    default:'',
    required:true
  },
  dialogVisible:{
    type:Boolean,
    default:false,
    required:true
  }
})

const form=ref({
  id:-1,
  gonghuoshangmingchen:"",
  farenxinming:"",
  farensfz:"",
  dizhi:""
})

const formRef=ref(null)

const emits=defineEmits(['update:modelValue','initGhsList'])

/**
 * 规则验证
 * */
const rules=ref({
  gonghuoshangmingchen:[
    { required: true, message: '请输入供货商名称'}
  ],
  farenxinming:[
    { required: true, message: '请输入法人姓名'}
  ],
  farensfz:[
    { required: true, message: '请输入身份证号',trigger: "blur"},
    { pattern: /^(\d{17}[\d|X|x]|\d{15})$/, message: "请输入正确的身份证", trigger: "blur" }
  ],
  dizhi:[
    { required: true, message: '请输入地址' }
  ]
})

/**
 * 根据id查询对应用户的信息
 * */
const initFormData=async(id)=>{
  const res=await requestUtil.get("sys/ghs/"+id)
  form.value=res.data.sysGonghuoshan
}

/**
 * 关闭dialog
 * */
const handleClose=()=>{
  emits('update:modelValue',false)
}

watch(
    ()=>props.dialogVisible,
    ()=>{
      let id=props.ghsid
      if(id!=-1){
        initFormData(id)
      }else{
        form.value={
          id:-1,
          gonghuoshangmingchen:"",
          farenxinming:"",
          farensfz:"",
          dizhi:""
        }
      }
    }
)

/**
 * 提交
 * */
const handleConfirm=()=>{
  formRef.value.validate(async(valid)=>{
    if(valid){
      let result=await requestUtil.post("sys/ghs/save",form.value)
      let data=result.data
      if(data.code==200){
        ElMessage.success("执行成功！")
        formRef.value.resetFields()
        emits("initGhsList")
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
