// src/views/MainView.vue
<template>
  <div class="p-4">
    <h1 class="text-2xl font-bold mb-4">메인 페이지</h1>
    <div class="bg-white rounded-lg shadow p-4">
      <p class="mb-2">로그인 성공!</p>
      <div v-if="authStore.user" class="text-gray-700">
        <p>이메일: {{ authStore.user.email }}</p>
        <p>이름: {{ authStore.user.userName }}</p>
        <p>역할: {{ authStore.user.role }}</p>
      </div>
      <button 
        @click="handleLogout" 
        class="mt-4 bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const handleLogout = async () => {
  try {
    await authStore.logout();
  } catch (error) {
    console.error('Logout failed:', error);
  }
};
</script>