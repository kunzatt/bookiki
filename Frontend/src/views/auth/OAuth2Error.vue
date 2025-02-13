<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

onMounted(() => {
  // URL에서 에러 메시지 가져오기
  const error = route.query.error;
  console.error('OAuth 로그인 에러:', error);
  
  // 필요한 경우 일정 시간 후 홈이나 로그인 페이지로 리다이렉트
  setTimeout(() => {
    router.push('/login');  // 또는 router.push('/');
  }, 3000);
});
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100">
    <div class="p-8 bg-white rounded-lg shadow-md">
      <h1 class="text-2xl font-bold text-red-600 mb-4">로그인 오류</h1>
      <p class="text-gray-600">
        로그인 처리 중 오류가 발생했습니다.
        <br>
        잠시 후 로그인 페이지로 이동합니다.
      </p>
    </div>
  </div>
</template>