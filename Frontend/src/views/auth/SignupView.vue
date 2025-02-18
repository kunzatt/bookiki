<!-- src/views/auth/SignUp.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { UserSignUpRequest } from '@/types/api/user';
import { checkCompanyId, registerWithEmail } from '@/api/user';
import { AUTH_MESSAGES } from '@/constants/messages';
import EmailVerify from '@/components/auth/EmailVerify.vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import Toast from '@/components/ui/Alert/ToastAlert.vue';

const router = useRouter();

// 폼 데이터
const formData = ref<UserSignUpRequest>({
  email: '',
  password: '',
  userName: '',
  companyId: '',
});

// 상태 관리
const isEmailVerified = ref(false);
const isCompanyIdVerified = ref(false);
const passwordConfirm = ref('');

// Toast 상태 관리
const toastConfig = ref({
  isVisible: false,
  message: '',
  type: 'info' as const,
});

// Toast 표시 함수
const showToast = (message: string, type: 'success' | 'info' = 'info') => {
  toastConfig.value = {
    isVisible: true,
    message,
    type,
  };
};

// 이메일 인증 버튼 클릭 시 이메일만 유효성 검사하도록 수정
const handleEmailVerifyClick = () => {
  if (!formData.value.email) {
    showToast(AUTH_MESSAGES.ERROR.INVALID_EMAIL);
    return;
  }
};

// 이메일 인증 완료 핸들러
const handleEmailVerified = () => {
  isEmailVerified.value = true;
  showToast(AUTH_MESSAGES.SUCCESS.EMAIL_VERIFIED, 'success');
};

// 사번 중복 확인
const checkCompanyIdDuplicate = async () => {
  if (!formData.value.companyId) {
    showToast(AUTH_MESSAGES.ERROR.FORM_INVALID);
    return;
  }

  try {
    await checkCompanyId(formData.value.companyId);
    isCompanyIdVerified.value = true;
    showToast(AUTH_MESSAGES.SUCCESS.COMPANY_ID_AVAILABLE, 'success');
  } catch (error) {
    isCompanyIdVerified.value = false;
    showToast(AUTH_MESSAGES.ERROR.COMPANY_ID_DUPLICATE);
  }
};

// 비밀번호 일치 여부 확인
const passwordMismatch = computed(() => {
  return passwordConfirm.value && formData.value.password !== passwordConfirm.value;
});

// 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    isEmailVerified.value &&
    isCompanyIdVerified.value &&
    formData.value.userName.length > 0 &&
    formData.value.password.length >= 8 &&
    !passwordMismatch.value
  );
});

// 회원가입 제출
const handleSubmit = async () => {
  if (!isFormValid.value) {
    showToast(AUTH_MESSAGES.ERROR.FORM_INVALID);
    return;
  }

  try {
    await registerWithEmail(formData.value);
    showToast(AUTH_MESSAGES.SUCCESS.SIGNUP, 'success');
    setTimeout(() => {
      router.push('/login');
    }, 1500); // Toast가 보이고 나서 이동
  } catch (error) {
    showToast(AUTH_MESSAGES.ERROR.SIGNUP_FAILED);
  }
};
</script>

<template>
  <div class="min-h-screen flex justify-center items-center p-4">
    <!-- 데스크탑 크기에서만 보이는 카드형 컨테이너 -->
    <div
      class="hidden md:flex flex-col w-full max-w-[480px] bg-white rounded-lg shadow-lg p-8 border border-gray-200"
    >
      <!-- 상단: 로고 영역 -->
      <div class="flex justify-center mb-8">
        <img src="@/assets/BookikiLogo.PNG" alt="Bookiki Logo" class="w-32" />
      </div>

      <!-- 회원가입 폼 영역 -->
      <div class="w-full">
        <h2 class="text-2xl font-bold mb-8">회원가입</h2>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- 이메일 인증 -->
          <EmailVerify
            v-model="formData.email"
            @verified="handleEmailVerified"
            @click="handleEmailVerifyClick"
          />

          <!-- 이름 -->
          <BasicInput v-model="formData.userName" type="full" placeholder="Name" label="이름" />

          <!-- 사번 -->
          <BasicInput
            v-model="formData.companyId"
            type="withButton"
            placeholder="Company ID"
            label="사번"
            button-text="중복 확인"
            @button-click="checkCompanyIdDuplicate"
          />

          <!-- 비밀번호 -->
          <BasicInput
            v-model="formData.password"
            type="password"
            placeholder="Password (비밀번호 형식 표기)"
          />

          <!-- 비밀번호 확인 -->
          <BasicInput v-model="passwordConfirm" type="password" placeholder="Password" />

          <!-- 회원가입 버튼 -->
          <BasicButton
            size="L"
            text="회원가입"
            :disabled="!isFormValid"
            @click="handleSubmit"
            class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
          />

          <!-- 로그인 링크 -->
          <div class="flex w-full justify-center">
            <button
              type="button"
              class="text-sm text-gray-500 hover:text-gray-700"
              @click="router.push('/login')"
            >
              이미 계정이 있으신가요? <span class="text-[#698469]">로그인</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 모바일 크기에서만 보이는 컨테이너 -->
    <div class="md:hidden w-full max-w-[384px]">
      <h2 class="text-2xl font-bold mb-8">회원가입</h2>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <!-- 이메일 인증 -->
        <EmailVerify v-model="formData.email" @verified="handleEmailVerified" />

        <!-- 이름 -->
        <BasicInput v-model="formData.userName" type="full" placeholder="Name" label="이름" />

        <!-- 사번 -->
        <BasicInput
          v-model="formData.companyId"
          type="withButton"
          placeholder="Company ID"
          label="사번"
          button-text="중복 확인"
          @button-click="checkCompanyIdDuplicate"
        />

        <!-- 비밀번호 -->
        <BasicInput
          v-model="formData.password"
          type="password"
          placeholder="Password (비밀번호 형식 표기)"
        />

        <!-- 비밀번호 확인 -->
        <BasicInput v-model="passwordConfirm" type="password" placeholder="Password" />

        <!-- 회원가입 버튼 -->
        <BasicButton
          size="L"
          text="회원가입"
          :disabled="!isFormValid"
          @click="handleSubmit"
          class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
        />

        <!-- 로그인 링크 -->
        <div class="flex w-full justify-center">
          <button
            type="button"
            class="text-sm text-gray-500 hover:text-gray-700"
            @click="router.push('/login')"
          >
            이미 계정이 있으신가요? <span class="text-[#698469]">로그인</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Toast 컴포넌트 -->
    <Toast v-bind="toastConfig" />
  </div>
</template>
