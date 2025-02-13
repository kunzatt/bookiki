<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  try {
    // URL에서 임시 토큰 추출
    const temporaryToken = route.query.token as string;

    if (!temporaryToken) {
      throw new Error('임시 토큰이 없습니다.');
    }

    // 첫 로그인인 경우 추가 정보 입력 페이지로 리다이렉트
    if (route.query.isFirstLogin === 'true') {
      router.push({
        path: '/oauth2/signup',
        query: { 
          token: temporaryToken 
        }
      });
      return;
    }

    // 기존 회원인 경우 바로 로그인 처리
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