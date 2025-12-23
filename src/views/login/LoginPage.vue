<template>
  <div class="body" style="padding: 0;width: 100vw;height: 100vh;">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="container" auto-complete="on"
             label-position="left" style="display: flex;flex-direction: column">
      <div class="title-container" style="float: left;display: flex;flex-direction: column;margin-bottom: 1rem">
        <!--        <img src="/favicon.ico" style="width: 3rem;height: 3rem;margin-bottom: 1rem">-->
        <div style="margin: auto;font-size: 1.7rem;">药店库房管理系统</div>
        <el-divider/>
        <div class="title">登录</div>
      </div>
      <el-form-item prop="username" style="width: 70%;margin: auto;" label="账号">
        <el-input
            ref="username"
            v-model="loginForm.username"
            placeholder="用户名"
            type="text"
            auto-complete="off"
            @keyup.enter="handleLogin"
        >
          <template #prefix>
            <svg-icon icon="user"></svg-icon>
          </template>
        </el-input>
      </el-form-item>


      <el-form-item prop="password" style="width:70%;margin: auto;" label="密码">
        <!-- :type="passwordType"-->
        <el-input
            ref="password"
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            auto-complete="off"
            show-password
            @keyup.enter="handleLogin"
        >
          <template #prefix>
            <svg-icon icon="password"></svg-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="rememberMe">
        <el-checkbox v-model="loginForm.rememberMe" label="记住密码" style="margin: auto;margin-top: 10px;"/>

      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" class="btn-login"
                   @click.prevent="handleLogin">登 录
        </el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script setup>
import {ref} from 'vue'
import requestUtil from '@/utils/request.js'
import store from '@/store'
import qs from 'qs'
import {ElMessage} from "element-plus"
import router from "@/router"
import Cookies from 'js-cookie'
import {encrypt, decrypt} from "@/utils/jsencrypt"

const loginRef = ref(null)

const loginForm = ref({
  // username: 'admin',
  // password: '123456',
  username: '',
  password: '',
  rememberMe: false
})

const validatePassword = (rule, value, callback) => {
  if (value.length < 6) {
    callback(new Error('密码不能少于6位'));
  } else {
    callback();
  }
}

const loginRules = {
  username: [
    {required: true, trigger: 'blur', message: "请输入账号"}
  ],
  password: [
    {required: true, trigger: 'blur', message: "请输入不能少于6位密码", validator: validatePassword}
  ]
};

function getCookie() {
  const username = Cookies.get("username");
  const password = Cookies.get("password");
  const rememberMe = Cookies.get("rememberMe");
  if (rememberMe === 'true') {
    loginForm.value = {
      username: username === undefined ? loginForm.value.username : username,
      password: password === undefined ? loginForm.value.password : decrypt(password),
      rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
    }
  }
}

getCookie();

const loading = ref(false);

function islogin(){
  if(store.getters.GET_LOGINTAB==='true'){
    router.replace('/')
  }
}

islogin()

const handleLogin = () => {
  loginRef.value.validate(async (valid) => {
    if (valid) {
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, {expires: 30});//30天
        Cookies.set("password", encrypt(loginForm.value.password), {expires: 30});
        Cookies.set("rememberMe", loginForm.value.rememberMe, {expires: 30});
      } else {
        // 否则移除
        Cookies.remove("username");
        Cookies.remove("password");
        Cookies.remove("rememberMe");
      }

      let result = await requestUtil.post("login?" + qs.stringify(loginForm.value))
      loading.value=true
      let data = result.data
      if (data.code === 200) {
        const token = data.authorization
        const menuList = data.menuList
        const currentUser = data.currentUser
        await store.commit('SET_TOKEN', token)
        await store.commit('SET_MENULIST', menuList)
        await store.commit('SET_USERINFO', currentUser)
        await store.commit('SET_LOGINTAB', true)
        location.reload()
      } else {
        loading.value=false
        ElMessage.error(data.msg)
      }
    } else {
      console.log('error submit!!');
    }
  })

}

</script>

<style lang="scss" scoped>
$bg: #283443;
$light_gray: #fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.login-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 100%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}

.body {
  /* 100%窗口宽高 */
  height: 100vh;
  display: flex;
  /* 弹性布局 水平+垂直居中 */
  padding: 0;
  margin: 0;
  background-image: url('https://zzpbzx-1316673662.cos.ap-nanjing.myqcloud.com/finance/BingWallpaper.jpg');
  background-size: cover;
}

.container {
  width: 30rem;
  height: 20rem;
  background-color: #fff;
  box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px;
  border-radius: 10px;
  padding: 2rem;
  right: 5rem;
  top: 13rem;
  position: absolute;
}

/* 当屏幕宽度小于等于480px时的样式 */
@media (max-width: 480px) {
  .container {
    width: 50%;
    height: 50%;
    margin: 0.1rem;
    border-radius: 0;
  }
}

/* 登录按钮 */
.btn-login {
  width: 70%;
  height: 50px;
  margin: auto;
  border: none;
  outline: none;
  color: #fff;
  border-radius: 5px;
  font-size: 18px;
  letter-spacing: 8px;
  text-indent: 8px;
  cursor: pointer;
}

.show-pwd {
  position: absolute;
  right: 10px;
  font-size: 16px;
  color: #397bce;
  cursor: pointer;
  user-select: none;
}

.title-container {
  position: relative;

  .title {
    font-size: 1.5rem;
    margin: auto;
    text-align: center;
    font-weight: bold;
  }
}
</style>
