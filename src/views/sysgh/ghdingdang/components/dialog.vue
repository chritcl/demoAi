<template>
  <el-dialog model-value="dialogVisible" :title="dialogTitle" width="55%" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row>
        <el-form-item label="订单编号" prop="yaopinbianhao">
          <el-input v-model="form.id" disabled/>
        </el-form-item>
        <el-form-item label="药品编号" prop="yaopinmingcheng">
          <el-input v-model="form.yaopingid" disabled/>
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-input v-model="form.status" disabled/>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item label="供货商名称" prop="gonghuoshanmingchen">
          <el-input v-model="form.gonghuoshanmingchen" disabled/>
        </el-form-item>
        <el-form-item label="员工名称" prop="yuangongmingchen">
          <el-input v-model="form.yuangongmingchen" disabled/>
        </el-form-item>
        <el-form-item label="支付状态" prop="pay">
          <el-input v-model="form.pay" disabled/>
        </el-form-item>
      </el-row>
<!--      <el-row>-->
<!--        <el-form-item label="审核状态" prop="shenghezhuangtai">-->
<!--          <el-switch v-model="form.shenghezhuangtai" active-text="审核" inactive-text="未审核" active-value="1" inactive-value="0" disabled/>-->
<!--        </el-form-item>-->
<!--      </el-row>-->
      <el-col>
        <el-form-item label="采购数量" prop="caigoushuliang">
          <el-input v-model="form.caigoushuliang"/>
        </el-form-item>
      </el-col>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea"/>
      </el-form-item>
      <el-form-item label="审核备注" prop="shenghebeizhu">
        <el-input v-model="form.shenghebeizhu" type="textarea"/>
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
  id:{
    type:String,
    default:'-1',
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
  id: '-1',
  remark: "",
  yaopingid: -1,
  ygid: -1,
  ghsid: -1,
  caigoushuliang: 0,
  pay: "",
  status: "",
  shenghezhuangtai: "",
  shenghebeizhu: null,
  gonghuoshanmingchen: "",
  yuangongmingchen: ""
})

const formRef=ref(null)

const emits=defineEmits(['update:modelValue','initMedicineList'])

/**
 * 规则验证
 * */
const rules=ref({
  caigoushuliang:[
    { required: true, message: '请输入采购数量'}
  ],
  code:[
    { required: true, message: '请输入权限字符' }
  ]
})

/**
 * 根据id查询对应用户的信息
 * */
const initFormData=async(id)=>{
  const res=await requestUtil.get("sys/ghdingdang/"+id)
  form.value=res.data.sysCaigoudingdan
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
      let id=props.id
      if(id!='-1'){
        initFormData(id)
      }else{
        form.value={
          id: '-1',
          remark: "",
          yaopingid: -1,
          ygid: -1,
          ghsid: -1,
          caigoushuliang: 0,
          pay: "",
          status: "",
          shenghezhuangtai: "",
          shenghebeizhu: null,
          gonghuoshanmingchen: "",
          yuangongmingchen: ""
        }
      }
    }
)

/**
 * 提交
 * */
const handleConfirm=()=>{
  console.log(form.value)
  formRef.value.validate(async(valid)=>{
    if(valid){
      let result=await requestUtil.post("sys/ghdingdang/save",form.value)
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
