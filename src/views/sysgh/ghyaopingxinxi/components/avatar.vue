<template>
  <el-form
      ref="formRef"
      :model="form"
      label-width="100px"
      style="text-align: center;padding-bottom:10px"
  >
    <el-upload
        :headers="headers"
        class="avatar-uploader"
        :action="getServerUrl()+'sys/ghmedicine/uploadImage'"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :before-upload="beforeAvatarUpload"
    >
      <img v-if="imageUrl" :src="imageUrl" class="avatar" />
      <el-icon v-else class="avatar-uploader-icon"><Plus/></el-icon>
    </el-upload>
    <el-button @click="handleConfirm">确认更换</el-button>
  </el-form>
</template>

<script setup>
import {ref, defineProps, watch} from "vue"
import requestUtil,{getServerUrl} from "@/utils/request"
import { ElMessage } from 'element-plus'
import store from "@/store"

const props=defineProps({
  id:{
    type:Number,
    default:-1,
    required:true
  }
})

const form=ref({
  id:-1,
  picture:""
})

const imageUrl=ref("")

watch(
    ()=> props.id,
    ()=>{
      let id=props.id
      if(id!=-1){
        initFormData(id)
      }else{
        form.value.picture='null.png'
        imageUrl.value = getServerUrl() + 'image/medicinepic/' + form.value.picture
      }
    }
)

const initFormData=async(id)=>{
  if(id!=-1) {
    const res = await requestUtil.get("sys/ghmedicine/" + id)
    form.value.picture = res.data.sysYaopinxinxi.picture
    form.value.id = res.data.sysYaopinxinxi.id
    imageUrl.value = getServerUrl() + 'image/medicinepic/' + form.value.picture
  }else{
    form.value.picture='null.png'
    imageUrl.value = getServerUrl() + 'image/medicinepic/' + form.value.picture
  }
}

initFormData(props.id)

const headers=ref({
  token:store.getters.GET_TOKEN
})

const handleAvatarSuccess=(res)=>{
  imageUrl.value=getServerUrl()+res.data.src
  form.value.picture=res.data.title;
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('图片必须是jpg格式')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2M!')
  }
  return isJPG && isLt2M
}

const handleConfirm=async()=>{
  console.log(form)
  let result=await requestUtil.post("sys/ghmedicine/updateImg",form.value);
  let data=result.data;
  if(data.code==200){
    ElMessage.success("执行成功！")
  }else{
    ElMessage.error(data.msg);
  }
}

</script>

<style scoped lang="scss">
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
.avatar {
  width: 160px;
  display: block;
}
</style>
