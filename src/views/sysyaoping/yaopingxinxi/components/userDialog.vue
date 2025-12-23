<template>
  <el-dialog model-value="userDialogVisible" title="账户权限" width="20%" @close="handleClose">
    <p>请选择供应商</p>
    <el-select
        v-model="ghs_form_value"
        filterable
        placeholder="请选择供应商"
        style="width: 240px"
    >
      <el-option
          v-for="item in ghs_form"
          :key="item.userId"
          :label="item.username"
          :value="item.userId"
      />
    </el-select>
    <p>请选择员工</p>
    <el-select
        v-model="yg_form_value"
        filterable
        placeholder="请选择员工"
        style="width: 240px"
    >
      <el-option
          v-for="item in yg_form"
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
import requestUtil, {getServerUrl} from "@/utils/request"
import {ElMessage} from 'element-plus'

const props = defineProps({
  id: {
    type: Number,
    default: -1,
    required: true
  },
  yuangongid: {
    type: Number,
    default: -1,
    required: true
  },
  gonghuoshangid: {
    type: Number,
    default: -1,
    required: true
  },
  userDialogVisible: {
    type: Boolean,
    default: false,
    required: true
  }
})

const ghs_form = ref([])
const ghs_form_value = ref()
const yg_form = ref([])
const yg_form_value = ref()
const value = ref()

const initFormData = async () => {
  const res = await requestUtil.post("sys/medicine/search_ghs_yg")
  ghs_form.value = res.data.sysUserRoleList_ghs
  yg_form.value = res.data.sysUserRoleList_yg
  ghs_form_value.value = props.gonghuoshangid
  yg_form_value.value = props.yuangongid
  value.value = props.id
}

watch(
    () => props.userDialogVisible,
    () => {
      let id = props.id;
      // console.log("id="+id)
      if (id != -1) {
        initFormData()
      }
    }
)

const emits = defineEmits(['update:modelValue', 'initMedicineList'])

/**
 * 关闭dialog
 * */
const handleClose = () => {
  emits('update:modelValue', false)
}

/**
 * 保存
 * */
const handleConfirm = () => {
  if (yg_form_value.value === null) {
    ElMessage.error('请选择员工，再进行保存！')
  } else if (ghs_form_value.value === null) {
    ElMessage.error("请选择供货商，再进行保存！")
  } else {
    if (value.value !== "") {
      requestUtil.post("sys/medicine/updateghs_yg/" + value.value + "/" + ghs_form_value.value+"/"+yg_form_value.value).then(result => {
        let data = result.data
        if (data.code == 200) {
          ElMessage.success("执行成功！")
          emits("initMedicineList")
          handleClose()
        } else {
          ElMessage.error(data.msg)
        }
      })
    } else {
      ElMessage.error("数据异常，请联系管理员！")
    }
  }
}

</script>


<style scoped lang="scss">

</style>
