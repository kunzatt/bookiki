<!-- App.vue -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  // localStorage에서 토큰 확인
  const token = localStorage.getItem('token')
  if (token) {
    // 토큰이 있으면 user 정보도 복원
    const userStr = localStorage.getItem('user')
    if (userStr) {
      authStore.user = JSON.parse(userStr)
    }
  }
})
</script>

<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<style>
/* 전역 스타일이 필요하다면 여기에 추가 */
#app {
  @apply min-h-screen;
}
</style>