<script setup lang="ts">
const foundations = [
  { title: 'H5 工程', detail: 'uni-app 与 Vue3 已完成基础配置' },
  { title: '接口边界', detail: '后续业务页面将通过统一后端 API 接入' },
  { title: '交付范围', detail: '当前不预置任何业务功能或模拟数据' }
];
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="hero__eyebrow">工程基础</text>
      <text class="hero__title">协同办公平台</text>
      <text class="hero__description">移动 H5 开发入口已就绪，业务能力将在对应工作包中逐项实现。</text>
    </view>
    <view class="foundation-list">
      <view v-for="foundation in foundations" :key="foundation.title" class="foundation-card">
        <text class="foundation-card__title">{{ foundation.title }}</text>
        <text class="foundation-card__detail">{{ foundation.detail }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32rpx;
  box-sizing: border-box;
  background: #f6f8fb;
}

.hero {
  padding: 48rpx 36rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #1d4ed8, #2563eb);
  color: #ffffff;
}

.hero__eyebrow,
.hero__title,
.hero__description,
.foundation-card__title,
.foundation-card__detail {
  display: block;
}

.hero__eyebrow {
  margin-bottom: 16rpx;
  font-size: 24rpx;
  opacity: 0.8;
}

.hero__title {
  margin-bottom: 16rpx;
  font-size: 44rpx;
  font-weight: 700;
}

.hero__description {
  font-size: 28rpx;
  line-height: 1.7;
}

.foundation-list {
  display: grid;
  gap: 24rpx;
  margin-top: 24rpx;
}

.foundation-card {
  padding: 32rpx;
  border-radius: 20rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.foundation-card__title {
  margin-bottom: 12rpx;
  color: #172033;
  font-size: 32rpx;
  font-weight: 600;
}

.foundation-card__detail {
  color: #64748b;
  font-size: 28rpx;
  line-height: 1.7;
}
</style>
