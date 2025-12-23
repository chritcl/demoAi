<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24" :sm="24" :md="24" :lg="6">
        <el-card class="box-card">
          <template v-slot:header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>
          <div>
            <div class="text-center">
              <Avatar/>
            </div>
            <div>
              <ul class="list-group list-group-striped" style="list-style-type: none;">
                <li class="list-group-item">
                  <svg-icon icon="user" />&nbsp;&nbsp;用户名称
                  <div class="pull-right">{{currentUser.username}}</div>
                </li>
                <li class="list-group-item">
                  <el-icon><Cellphone /></el-icon>&nbsp;&nbsp;手机号码
                  <div class="pull-right">{{currentUser.phonenumber}}</div>
                </li>
                <li class="list-group-item">
                  <svg-icon icon="email" />&nbsp;&nbsp;用户邮箱
                  <div class="pull-right">{{currentUser.email}}</div>
                </li>
                <li class="list-group-item">
                  <svg-icon icon="roles" />&nbsp;&nbsp;所属角色
                  <div class="pull-right">{{currentUser.roles}}</div>
                </li>
                <li class="list-group-item">
                  <svg-icon icon="date" />&nbsp;&nbsp;创建日期
                  <div class="pull-right">{{formatDate(currentUser.loginDate)}}</div>
                </li>
              </ul>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template v-slot:header>
            <div class="clearfix">
              <span>修改</span>
            </div>
          </template>
          <el-tabs v-model="activeTab" type="card">
            <el-tab-pane label="基本资料" name="userinfo">
              <UserInfo/>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <ResetPwd :user="currentUser"/>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import Avatar from './avatar'
import UserInfo from './userInfo'
import ResetPwd from './resetpassword'
import { ref ,provide } from 'vue'
import store from '@/store'
import { formatDate } from '@/utils/formatDate'
import { Cellphone } from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'

const currentUser = ref(store.getters.GET_USERINFO);

provide("currentUser",currentUser);

const activeTab = ref("userinfo");

const loadingInstance1 = ElLoading.service({ fullscreen: true })
setTimeout(()=>{
  loadingInstance1.close()
},1000)
// nextTick(() => {
//   // Loading should be closed asynchronously
//   loadingInstance.close()
// })

</script>

<style lang="scss" scoped>
.list-group{
padding: 0rem 1rem;
}

.list-group-item {
  border-bottom: 1px solid #e7eaec;
  border-top: 1px solid #e7eaec;
  padding: 11px 0;
  font-size: 13px;
}

.pull-right{
  float: right!important;
}

::v-deep .el-card__body{
  height:230px;
}

::v-deep .box-card{
  height:500px;
}

</style>
