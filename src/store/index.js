import { createStore } from 'vuex'
import router from '@/router'

export default createStore({
  state: {
    hasRoutes:false
  },
  getters: {
    GET_TOKEN:state => {
      return sessionStorage.getItem("token");
    },
    GET_MENULIST:state => {
      return JSON.parse(sessionStorage.getItem("menuList"));
    },
    GET_USERINFO:state => {
      return JSON.parse(sessionStorage.getItem("userInfo"));
    },
    GET_LOGINTAB:state => {
      return sessionStorage.getItem("logintab");
    }
  },
  mutations: {
    SET_TOKEN:(state,token)=>{
      sessionStorage.setItem("token",token);
    },
    SET_MENULIST:(state,menuList)=>{
      sessionStorage.setItem("menuList",JSON.stringify(menuList));
    },
    SET_USERINFO:(state,userInfo)=>{
      sessionStorage.setItem("userInfo",JSON.stringify(userInfo));
    },
    SET_ROUTES_STATE:(state,hasRoutes)=>{
      state.hasRoutes=hasRoutes;
    },
    SET_LOGINTAB:(state,logintab)=>{
      sessionStorage.setItem("logintab",logintab);
    }
  },
  actions: {
    //安全退出
    logout(){
      window.sessionStorage.clear();
      this.state.hasRoutes=false
      router.replace("/login");
      location.reload();
    }
  },
  modules: {
  }
})
