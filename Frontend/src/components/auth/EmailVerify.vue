<!-- src/components/auth/EmailVerify.vue -->
<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { checkEmailDuplicate, sendVerificationCode, verifyCode } from '@/api/user';
import type { SendEmailRequest, VerifyCodeRequest } from '@/types/api/user';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import { AUTH_MESSAGES } from '@/constants/messages';
import Toast from '@/components/ui/Alert/ToastAlert.vue';

const props = defineProps<{
  modelValue: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'verified'): void;
  (e: 'failed'): void; // 실패 이벤트 추가
}>();

// 상태 관리
const email = ref(props.modelValue);
const verificationCode = ref('');
const showVerificationInput = ref(false);
const isVerified = ref(false);

// 이메일 유효성 검사
const isValidEmail = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.value);
});

// v-model 연동
watch(email, (newValue) => {
  if (!isVerified.value) {
    // 인증되지 않은 경우에만 업데이트
    emit('update:modelValue', newValue);
  } else {
    // 인증된 후에는 이메일 값을 변경하지 못하도록 원래 값으로 되돌림
    email.value = props.modelValue;
  }
});

// Toast 상태 관리
const toastConfig = ref({
  isVisible: false,
  message: '',
  type: 'info' as const,
});

const showToast = (message: string, type: 'success' | 'info' = 'info') => {
  toastConfig.value = {
    isVisible: true,
    message,
    type,
  };
};

// 이메일 중복 확인 및 인증코드 발송
const handleEmailCheck = async () => {
  console.log('1. 이메일 체크 시작:', email.value);

  if (!isValidEmail.value) {
    showToast(AUTH_MESSAGES.ERROR.INVALID_EMAIL);
    emit('failed'); // 실패 이벤트 발생
    return;
  }

  try {
    console.log('2. 이메일 중복 체크 시작');
    await checkEmailDuplicate(email.value);
    console.log('3. 이메일 중복 체크 완료');

    console.log('4. 인증 코드 발송 시작');
    const request: SendEmailRequest = { email: email.value };
    await sendVerificationCode(request);
    console.log('5. 인증 코드 발송 완료');

    console.log('6. 인증 입력창 표시 설정');
    showVerificationInput.value = true;
    console.log('7. showVerificationInput 값:', showVerificationInput.value);

    showToast(AUTH_MESSAGES.SUCCESS.EMAIL_SENT, 'success');
  } catch (error) {
    console.error('에러 발생:', error);
    showToast(AUTH_MESSAGES.ERROR.EMAIL_DUPLICATE);
    emit('failed'); // 실패 이벤트 발생
  }
};
// const handleEmailCheck = async () => {
//   if (!isValidEmail.value) {
//     showToast(AUTH_MESSAGES.ERROR.INVALID_EMAIL);
//     return;
//   }

//   try {
//     await checkEmailDuplicate(email.value);
//     const request: SendEmailRequest = { email: email.value };
//     await sendVerificationCode(request);
//     showVerificationInput.value = true;
//     showToast(AUTH_MESSAGES.SUCCESS.EMAIL_SENT, 'success');
//   } catch (error) {
//     showToast(AUTH_MESSAGES.ERROR.EMAIL_DUPLICATE);
//   }
// };

// 인증 코드 확인
const handleVerifyCode = async () => {
  if (!verificationCode.value) {
    showToast(AUTH_MESSAGES.ERROR.FORM_INVALID);
    emit('failed'); // 실패 이벤트 발생
    return;
  }

  try {
    const request: VerifyCodeRequest = { code: verificationCode.value };
    await verifyCode(email.value, request);
    isVerified.value = true;
    emit('verified');
    showToast(AUTH_MESSAGES.SUCCESS.EMAIL_VERIFIED, 'success');
  } catch (error) {
    showToast(AUTH_MESSAGES.ERROR.INVALID_CODE);
    emit('failed'); // 실패 이벤트 발생
  }
};
</script>

<template>
  <div class="space-y-4">
    <!-- 이메일 입력 -->
    <BasicInput
      type="withButton"
      input-type="email"
      v-model="email"
      placeholder="E-mail"
      label="이메일"
      button-text="메일 인증"
      :disabled="isVerified || props.disabled"
      @button-click="handleEmailCheck"
    />

    <!-- 인증 코드 입력 -->
    <BasicInput
      v-if="showVerificationInput"
      type="withButton"
      v-model="verificationCode"
      placeholder="인증번호를 입력하세요"
      button-text="인증"
      @button-click="handleVerifyCode"
    />

    <!-- 인증 완료 메시지 -->
    <div v-if="isVerified" class="text-sm text-green-600">인증되었습니다.</div>

    <!-- Toast 컴포넌트 -->
    <Toast v-bind="toastConfig" />
  </div>
</template>
