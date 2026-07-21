<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :md="8">
        <el-card shadow="never" class="tree-card">
          <el-input v-model="keyword" placeholder="姓名/拼音/电话搜索" :prefix-icon="'Search'" @keyup.enter="doSearch" clearable style="margin-bottom:12px" />
          <div v-show="!searchMode">
            <el-tree :data="tree" :props="{ label: 'label', children: 'children' }" node-key="id" default-expand-all @node-click="onNodeClick">
              <template #default="{ data }">
                <span><el-icon v-if="data.type==='dept'"><component :is="'OfficeBuilding'" /></el-icon>
                <el-icon v-else><component :is="'User'" /></el-icon> {{ data.label }}</span>
              </template>
            </el-tree>
          </div>
          <div v-show="searchMode">
            <el-empty v-if="!results.length" description="无匹配联系人" :image-size="60" />
            <div class="contact-item" v-for="u in results" :key="u.id" @click="showUser(u)">
              <el-avatar :size="36">{{ (u.nickname || '').charAt(0) }}</el-avatar>
              <div class="ci-main"><div class="ci-name">{{ u.nickname }}</div><div class="ci-sub">{{ u.phone }}</div></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :md="16">
        <el-card shadow="never">
          <template #header><span>联系人详情</span></template>
          <el-empty v-if="!current" description="点击左侧选择联系人" />
          <div v-else class="detail">
            <el-avatar :size="80">{{ (current.nickname || '').charAt(0) }}</el-avatar>
            <h2 style="margin:12px 0 4px">{{ current.nickname }}</h2>
            <el-descriptions :column="1" border style="margin-top:12px">
              <el-descriptions-item label="账号">{{ current.username }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ ['男', '女', '未知'][current.gender] }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ current.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ current.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="拼音">{{ current.pinyin || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { contactsTree, searchContacts } from '@/api/contacts'

const tree = ref([])
const keyword = ref('')
const searchMode = ref(false)
const results = ref([])
const current = ref(null)

async function loadTree() { searchMode.value = false; const res = await contactsTree(); tree.value = res.data || [] }
async function doSearch() {
  if (!keyword.value) { searchMode.value = false; return }
  const res = await searchContacts(keyword.value); searchMode.value = true; results.value = res.data || []
}
function onNodeClick(data) {
  if (data.type === 'user') {
    current.value = { nickname: data.label, phone: data.phone, email: data.email, gender: data.gender, username: '', pinyin: '' }
  }
}
function showUser(u) { current.value = u }
onMounted(loadTree)
</script>

<style scoped>
.tree-card { height: calc(100vh - 110px); overflow: auto; }
.contact-item { display: flex; align-items: center; gap: 10px; padding: 8px; border-radius: 6px; cursor: pointer; }
.contact-item:hover { background: #f5f7fa; }
.ci-name { font-weight: 600; }
.ci-sub { font-size: 12px; color: #909399; }
.detail { text-align: center; }
</style>
