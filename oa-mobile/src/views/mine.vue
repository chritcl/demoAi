<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="我的" fixed />
    <div class="head">
      <van-image round width="60" height="60" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" />
      <div style="margin-left:14px">
        <div class="m-title" style="font-size:18px">{{ user.nickname || '用户' }}</div>
        <div class="muted">{{ (user.roles || []).join('，') || '普通用户' }}</div>
      </div>
    </div>
    <van-cell-group inset style="margin-top:12px">
      <van-cell title="我的待办" icon="todo-list-o" is-link @click="$router.push('/todo')" />
      <van-cell title="消息中心" icon="envelop-o" is-link @click="$router.push('/message')" />
      <van-cell title="通讯录" icon="contact" is-link @click="$router.push('/contacts')" />
    </van-cell-group>
    <div style="padding:24px">
      <van-button type="danger" block round @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { useUserStore } from '@/store/user'
import { logout } from '@/api'

const router = useRouter()
const user = useUserStore()
function onLogout() {
  showConfirmDialog({ title: '提示', message: '确定退出登录？' }).then(async () => {
    try { await logout() } catch (e) {}
    user.reset()
    router.replace('/login')
  })
}
</script>

<style scoped>
.head { display: flex; align-items: center; padding: 20px 16px; background: #fff; }
</style>
