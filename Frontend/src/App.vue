<!-- App.vue -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

onMounted(() => {
  // sessionStorage에서 유저 정보 복구
  authStore.initializeFromStorage()

  // 현재 경로가 인증이 필요한 페이지인데 인증되지 않은 경우 로그인 페이지로 리다이렉트
  const currentRoute = router.currentRoute.value
  if (currentRoute.meta.requiresAuth && !authStore.isAuthenticated) {
    router.push('/login')
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