<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
};

onMounted(async () => {
  try {
    const accessToken = getCookie('access_token');
    if (!accessToken) {
      throw new Error('액세스 토큰이 없습니다.');
    }

    // 토큰은 이미 쿠키에 설정되어 있으므로,
    // 여기서는 상태 업데이트만 수행
    await authStore.handleOAuth2Login(accessToken);

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
