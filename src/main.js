import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './router/permission.js'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'


// 国际化中文
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import SvgIcon from '@/assets/icons'
import DataVVue3 from '@kjgl77/datav-vue3'

const app = createApp(App)
app.use(ElementPlus, {
    locale: zhCn,
})
SvgIcon(app)
app.use(store)
app.use(router)
app.use(DataVVue3)
// app.use(ElementPlus)
app.mount('#app')
