<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import SocialLoginButtons from '@/components/ui/Button/SocialLoginButtons.vue';

const authStore = useAuthStore();
const email = ref('');
const password = ref('');

const handleLogin = async () => {
  try {
    await authStore.login({
      email: email.value,
      password: password.value
    });
  } catch (error) {
    console.error('Login failed:', error);
    // TODO: 에러 처리 (토스트 또는 알림)
  }
};

const handleForgotPassword = () => {
  // TODO: 비밀번호 찾기 페이지로 이동 또는 모달 표시
};

const handleSignUp = () => {
  // TODO: 회원가입 페이지로 이동
};
</script>

<template>
    <div class="min-h-screen flex justify-center items-center p-4">
      <!-- 데스크탑 크기에서만 보이는 카드형 컨테이너 -->
      <div class="hidden md:flex flex-col w-full max-w-[480px] bg-white rounded-lg shadow-lg p-8 border border-gray-200">
        <!-- 상단: 로고 영역 -->
        <div class="flex justify-center mb-8">
          <img src="@/assets/BookikiLogo.png" alt="Bookiki Logo" class="w-32" />
        </div>
  
        <!-- 하단: 로그인 폼 영역 -->
        <div class="w-full">
          <h2 class="text-2xl font-bold mb-8">Welcome!</h2>
  
          <form @submit.prevent="handleLogin" class="space-y-4">
            <BasicInput
              v-model="email"
              type="full"
              placeholder="ID"
              inputType="email"
            />
            
            <BasicInput
              v-model="password"
              type="password"
              placeholder="Password"
            />
  
            <div class="flex w-full justify-end">
              <button
                type="button"
                class="text-sm text-gray-500 hover:text-gray-700"
                @click="handleForgotPassword"
              >
                비밀번호를 잊으셨나요?
              </button>
            </div>
  
            <BasicButton
              size="L"
              text="로그인"
              @click="handleLogin"
              class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
            />
  
            <div class="flex w-full justify-center">
              <button
                type="button"
                class="text-sm text-gray-500 hover:text-gray-700"
                @click="handleSignUp"
              >
                회원이 아니신가요? <span class="text-[#698469]">회원가입</span>
              </button>
            </div>
  
            <!-- 데스크탑 버전 SNS 로그인 섹션 -->
            <div class="mt-8 flex flex-col items-center w-full">
            <p class="text-center text-sm text-gray-500 mb-4">
                SNS 계정으로 로그인
            </p>
            <div class="w-full"> <!-- flex justify-center 제거하고 w-full 추가 -->
                <SocialLoginButtons />
            </div>
            </div>
          </form>
        </div>
      </div>
  
      <!-- 모바일 크기에서만 보이는 기존 컨테이너 -->
      <div class="md:hidden w-full max-w-[384px]">
        <h2 class="text-2xl font-bold mb-8">Welcome!</h2>
        
        <form @submit.prevent="handleLogin" class="space-y-4">
          <!-- 기존 폼 내용과 동일 -->
          <BasicInput
            v-model="email"
            type="full"
            placeholder="ID"
            inputType="email"
          />
          
          <BasicInput
            v-model="password"
            type="password"
            placeholder="Password"
          />
  
          <div class="flex w-full justify-end">
            <button
              type="button"
              class="text-sm text-gray-500 hover:text-gray-700"
              @click="handleForgotPassword"
            >
              비밀번호를 잊으셨나요?
            </button>
          </div>
  
          <BasicButton
            size="L"
            text="로그인"
            @click="handleLogin"
            class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
          />
  
          <div class="flex w-full justify-center">
            <button
              type="button"
              class="text-sm text-gray-500 hover:text-gray-700"
              @click="handleSignUp"
            >
              회원이 아니신가요? <span class="text-[#698469]">회원가입</span>
            </button>
          </div>
  
        <!-- 모바일 버전 SNS 로그인 섹션 -->
        <div class="mt-8 flex flex-col items-center w-full">
            <p class="text-center text-sm text-gray-500 mb-4">
                SNS 계정으로 로그인
            </p>
            <div class="w-full"> <!-- flex justify-center 제거하고 w-full 추가 -->
                <SocialLoginButtons />
            </div>
        </div>
        </form>
      </div>
    </div>
  </template>