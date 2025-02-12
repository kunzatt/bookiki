<!-- App.vue -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  authStore.initialize()
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