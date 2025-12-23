<template>
  <el-dialog model-value="dialogVisible" :title="dialogTitle" width="55%" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-row>
        <el-form-item label="药品编号" prop="yaopinbianhao">
          <el-input v-model="form.yaopinbianhao" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
        <el-form-item label="药品名称" prop="yaopinmingcheng">
          <el-input v-model="form.yaopinmingcheng" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
        <el-form-item label="药品类别" prop="farensfz" >
          <el-select
              v-model="form.yaopinleibie"
              class="m-2"
              placeholder="Select"
              style="width: 240px"
              :disabled="dialogTitle==='药品信息查看'"
          >
            <el-option
                v-for="item in yaopinleibie"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-row>
      <el-row>
        <el-col :span="10">
          <el-form-item label="规格" prop="guige">
            <el-input v-model="form.guige" :disabled="dialogTitle==='药品信息查看'"/>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="生产日期" prop="shengchanriqi">
            <el-input type="datetime-local" v-model="form.shengchanriqi" :disabled="dialogTitle==='药品信息查看'"/>
          </el-form-item>
        </el-col>
        <el-form-item label="供货商" prop="gonghuoshanmingchen">
          <el-input v-model="form.gonghuoshanmingchen" disabled/>
        </el-form-item>
        <el-form-item label="员工" prop="yuangongmingchen">
          <el-input v-model="form.yuangongmingchen" disabled/>
        </el-form-item>
        <el-form-item label="批次号" prop="picihao">
          <el-input v-model="form.picihao" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
        <el-col :span="10">
          <el-form-item label="创建日期" prop="create_time">
            <el-input v-model="form.createTime" disabled/>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item label="更新日期" prop="update_time">
            <el-input v-model="form.updateTime" disabled/>
          </el-form-item>
        </el-col>
        <el-form-item label="数量" prop="shuliang">
          <el-input v-model="form.shuliang" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
        <el-form-item label="进货金额(元)" prop="payMoney">
          <el-input v-model="form.inMoney" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
        <el-form-item label="售卖金额(元)" prop="payMoney">
          <el-input v-model="form.payMoney" :disabled="dialogTitle==='药品信息查看'"/>
        </el-form-item>
      </el-row>
      <el-form-item label="注意事项" prop="remark">
        <el-input v-model="form.remark" type="textarea" :disabled="dialogTitle==='药品信息查看'"/>
      </el-form-item>
    </el-form>
    <div v-if="dialogTitle!=='药品信息查看'&&dialogTitle!=='药品信息新增'">
      <Avatar :id="imgid" v-model="imgid"/>
    </div>
    <template #footer>
      <span class="dialog-footer" v-if="dialogTitle==='药品信息修改'||dialogTitle==='药品信息新增'">
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
import Avatar from "@/views/sysgh/ghyaopingxinxi/components/avatar"
import store from "@/store"

const currentUser = ref(store.getters.GET_USERINFO);

const props=defineProps({
  id:{
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
  yaopinbianhao:"",
  yaopinmingcheng:"",
  yaopinleibie:"",
  guige:"",
  picture:"",
  picihao:"",
  shengchanriqi:"",
  gonghuoshanmingchen:"",
  yuangongmingchen:"",
  shuliang:"",
  inMoney:"",
  payMoney:"",
  createTime:"",
  updateTime:"",
  remark:""
})

const yaopinleibie=[
    { value: '处方药', label: '处方药'},
    { value: '非处方药', label: '非处方药'}
]

const formRef=ref('')

const emits=defineEmits(['update:modelValue','initMedicineList'])

/**
 * 规则验证
 * */
const rules=ref({
  yaopinbianhao:[
    { required: true, message: '请输入药品编号'}
  ],
  yaopinmingcheng:[
    { required: true, message: '请输入药品名称' }
  ],
  guige:[
    { required: true, message: '请输入药品规格' }
  ],
  shengchanriqi:[
    { required: true, message: '请输入药品生产日期' }
  ],
  picihao:[
    { required: true, message: '请输入药品批次' }
  ],
  shuliang:[
    { required: true, message: '请输入药品数量' }
  ],
  inMoney:[
    { required: true, message: '请输入药品进价' }
  ],
  payMoney:[
    { required: true, message: '请输入药品售价' }
  ]
})

/**
 * 根据id查询对应信息
 * */
const initFormData=async(id)=>{
  const res=await requestUtil.get("sys/ghmedicine/"+id)
  form.value=res.data.sysYaopinxinxi
}

/**
 * 关闭dialog
 * */
const handleClose=()=>{
  emits('update:modelValue',false)
}

const imgid=ref()

watch(
    ()=>props.dialogVisible,
    ()=>{
      let id=props.id
      if(id!=-1){
        initFormData(id)
        imgid.value=id
      }else{
        form.value={
          id:-1,
          yaopinbianhao:"",
          yaopinmingcheng:"",
          yaopinleibie:"",
          guige:"",
          picture:"",
          picihao:"",
          shengchanriqi:"",
          gonghuoshanmingchen:"",
          yuangongmingchen:"",
          shuliang:"",
          payMoney:"",
          inMoney:"",
          createTime:"",
          updateTime:"",
          remark:""
        }
        imgid.value=-1
      }
    }
)

/**
 * 提交
 * */
const handleConfirm=()=>{
  formRef.value.validate(async(valid)=>{
    if(valid){
      form.value.shengchanriqi = new Date(form.value.shengchanriqi).toISOString()
      const res=await requestUtil.get("sys/ghmedicine/"+props.id)
      if(props.dialogTitle==='药品信息新增'){
        form.value.picture='null.png'
      }else{
        form.value.picture=res.data.sysYaopinxinxi.picture
      }
      const commitform={
        id:form.value.id,
        shengchanriqi: form.value.shengchanriqi,
        yaopinbianhao:form.value.yaopinbianhao,
        yaopinmingcheng:form.value.yaopinmingcheng,
        yaopinleibie:form.value.yaopinleibie,
        picture:form.value.picture,
        guige:form.value.guige,
        picihao:form.value.picihao,
        shuliang:form.value.shuliang,
        payMoney:form.value.payMoney,
        inMoney:form.value.inMoney,
        gonghuoshangid:form.value.gonghuoshanmingchen===''?currentUser.value.id:res.data.sysYaopinxinxi.gonghuoshangid,
        remark:form.value.remark
      }
      let result=await requestUtil.post("sys/ghmedicine/save",commitform)
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
