<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { resetPassword } from '@/api/user';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';

const router = useRouter();
const route = useRoute();
const newPassword = ref('');
const confirmPassword = ref('');
const error = ref('');
const success = ref(false);

// 비밀번호 유효성 검사 함수
const validatePassword = (password: string): { isValid: boolean; message: string } => {
  if (password.length < 8 || password.length > 20) {
    return {
      isValid: false,
      message: '비밀번호는 8~20자 사이여야 합니다.',
    };
  }

  const hasLetter = /[a-zA-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

  if (!hasLetter || !hasNumber || !hasSpecial) {
    return {
      isValid: false,
      message: '비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.',
    };
  }

  return {
    isValid: true,
    message: '',
  };
};

const handleSubmit = async () => {
  try {
    error.value = '';

    // 새 비밀번호 유효성 검사
    const validation = validatePassword(newPassword.value);
    if (!validation.isValid) {
      error.value = validation.message;
      return;
    }

    // 새 비밀번호 확인 검사
    if (newPassword.value !== confirmPassword.value) {
      error.value = '새 비밀번호가 일치하지 않습니다.';
      return;
    }

    const token = route.params.token as string;
    if (!token) {
      error.value = '유효하지 않은 접근입니다.';
      return;
    }

    // 비밀번호 재설정 API 호출
    await resetPassword(token, {
      newPassword: newPassword.value,
      newPasswordConfirm: confirmPassword.value
    });

    success.value = true;
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  } catch (e: any) {
    console.error('비밀번호 재설정 실패:', e);
    if (e.response?.status === 400) {
      if (e.response?.data?.message) {
        error.value = e.response.data.message;
      } else if (e.response?.data?.code === 'INVALID_TOKEN') {
        error.value = '유효하지 않거나 만료된 링크입니다. 비밀번호 재설정을 다시 요청해주세요.';
      } else {
        error.value = '비밀번호 재설정에 실패했습니다.';
      }
    } else {
      error.value = '비밀번호 재설정에 실패했습니다. 다시 시도해주세요.';
    }
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 헤더 -->
    <div class="bg-white border-b">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <h1 class="text-2xl font-bold">비밀번호 재설정</h1>
        <p class="mt-2 text-sm text-gray-600">
          이메일로 받은 링크를 통해 새로운 비밀번호를 설정할 수 있습니다.
          링크는 10분간 유효하며, 1회만 사용 가능합니다.
        </p>
      </div>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="flex-1 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="bg-white rounded-lg shadow-sm p-6 space-y-6">
          <div v-if="success" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
            비밀번호가 성공적으로 재설정되었습니다. 로그인 페이지로 이동합니다.
          </div>

          <div v-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {{ error }}
          </div>

          <form @submit.prevent="handleSubmit" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">새 비밀번호</label>
              <BasicInput
                v-model="newPassword"
                type="password"
                placeholder="새 비밀번호를 입력하세요"
              />
              <p class="mt-1 text-sm text-gray-500">
                비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다.
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">새 비밀번호 확인</label>
              <BasicInput
                v-model="confirmPassword"
                type="password"
                placeholder="새 비밀번호를 다시 입력하세요"
              />
            </div>

            <div class="flex justify-end">
              <BasicButton
                type="submit"
                size="M"
                text="비밀번호 재설정"
                class="!bg-[#698469] !text-white hover:!bg-[#4a5d4a]"
              />
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>