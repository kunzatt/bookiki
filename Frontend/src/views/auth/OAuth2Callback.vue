<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  try {
    const token = route.query.token as string;
    if (!token) {
      throw new Error('토큰이 없습니다.');
    }

    await authStore.handleOAuth2Login(token);
    router.push('/main');
  } catch (error) {
    console.error('OAuth2 콜백 처리 중 오류:', error);
    router.push('/login');
  }
});
</script>

<template>
  <div class="flex justify-center items-center min-h-screen">
    <div>로그인 처리 중...</div>
  </div>
</template>
