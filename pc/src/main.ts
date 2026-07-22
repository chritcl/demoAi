import { createApp } from 'vue';
import {
  ElAside,
  ElButton,
  ElCard,
  ElContainer,
  ElHeader,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElTag
} from 'element-plus';
import 'element-plus/theme-chalk/base.css';
import 'element-plus/es/components/aside/style/css';
import 'element-plus/es/components/button/style/css';
import 'element-plus/es/components/card/style/css';
import 'element-plus/es/components/container/style/css';
import 'element-plus/es/components/header/style/css';
import 'element-plus/es/components/main/style/css';
import 'element-plus/es/components/menu/style/css';
import 'element-plus/es/components/menu-item/style/css';
import 'element-plus/es/components/tag/style/css';
import { createPinia } from 'pinia';
import App from './App.vue';
import { router } from './router';
import './styles/base.css';

const application = createApp(App);

application
  .use(createPinia())
  .use(router)
  .use(ElAside)
  .use(ElButton)
  .use(ElCard)
  .use(ElContainer)
  .use(ElHeader)
  .use(ElMain)
  .use(ElMenu)
  .use(ElMenuItem)
  .use(ElTag)
  .mount('#app');
