<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { completeOAuth2SignUp } from '@/api/user';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';

const route = useRoute();
const router = useRouter();

const userName = ref('');
const companyId = ref('');
const token = route.query.token as string;
const provider = route.params.provider as string;

const handleSignUp = async () => {
  try {
    await completeOAuth2SignUp(
      provider, 
      {
        userName: userName.value,
        companyId: companyId.value
      }, 
      token
    );
    
    router.push('/main');
  } catch (error) {
    console.error('OAuth2 회원가입 실패:', error);
    // 오류 처리 로직 추가
  }
};
</script>

<template>
  <div class="min-h-screen flex justify-center items-center">
    <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
      <h2 class="text-2xl font-bold text-center">추가 정보 입력</h2>
      <form @submit.prevent="handleSignUp" class="space-y-4">
        <BasicInput
          v-model="userName"
          type="full"
          placeholder="이름"
          label="이름"
          required
        />
        
        <BasicInput
          v-model="companyId"
          type="full"
          placeholder="사번"
          label="사번"
          required
        />
        
        <BasicButton
          type="submit"
          size="L"
          text="가입 완료"
          class="w-full"
        />
      </form>
    </div>
  </div>
</template>