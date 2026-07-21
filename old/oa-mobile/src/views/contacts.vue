<template>
  <div class="page" style="padding-top:46px">
    <van-nav-bar title="通讯录" fixed />
    <van-search v-model="keyword" placeholder="姓名/拼音/电话" @search="onSearch" shape="round" />
    <van-index-bar v-if="!mode">
      <div v-for="g in groups" :key="g.letter">
        <van-index-anchor :index="g.letter" />
        <van-cell v-for="u in g.users" :key="u.id" :title="u.nickname" :label="u.phone" is-link @click="onUser(u)">
          <template #icon><van-icon name="user-o" style="margin-right:8px;font-size:18px" /></template>
        </van-cell>
      </div>
      <van-empty v-if="!groups.length" description="暂无联系人" />
    </van-index-bar>
    <div v-else>
      <van-cell v-for="u in results" :key="u.id" :title="u.nickname" :label="u.phone" is-link @click="onUser(u)" />
      <van-empty v-if="!results.length" description="无匹配联系人" />
    </div>

    <van-dialog v-model="showDetail" :title="current.nickname" show-cancel-button cancel-button-text="关闭" confirm-button-text="拨打">
      <div style="padding:16px;line-height:1.8">
        <div>账号：{{ current.username }}</div>
        <div>电话：{{ current.phone || '-' }}</div>
        <div>邮箱：{{ current.email || '-' }}</div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const keyword = ref('')
const mode = ref('')
const tree = ref([])
const results = ref([])
const showDetail = ref(false)
const current = ref({})

const groups = computed(() => {
  const all = []
  collect(tree.value, all)
  // 按拼音首字母分组
  const map = {}
  all.forEach((u) => {
    const py = u.pinyin || u.nickname || ''
    const letter = py.charAt(0).toUpperCase()
    const k = /[A-Z]/.test(letter) ? letter : '#'
    ;(map[k] = map[k] || []).push(u)
  })
  return Object.keys(map).sort().map((letter) => ({ letter, users: map[letter] }))
})
function collect(nodes, out) {
  nodes.forEach((n) => {
    if (n.type === 'user') {
      out.push({ id: n.userId, nickname: n.label, phone: n.phone, email: n.email, pinyin: '' })
    } else if (n.children) collect(n.children, out)
  })
}

async function onSearch() {
  if (!keyword.value) { mode.value = ''; return }
  const { searchContacts } = await import('@/api')
  results.value = (await searchContacts(keyword.value)).data || []
  mode.value = 'search'
}
function onUser(u) { current.value = u; showDetail.value = true }

onMounted(async () => {
  const { contactsTree } = await import('@/api')
  tree.value = (await contactsTree()).data || []
})
</script>
