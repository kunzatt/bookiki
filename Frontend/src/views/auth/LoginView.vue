<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import SocialLoginButtons from '@/components/ui/Button/SocialLoginButtons.vue';
import { sendPasswordResetEmail } from '@/api/user';

const router = useRouter();
const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const resetEmail = ref('');
const resetName = ref(''); // 추가: 이름 입력을 위한 ref
const showResetModal = ref(false);
const showConfirmModal = ref(false);
const resetError = ref('');
const showLoginErrorModal = ref(false);
const resetButtonDisabled = ref(false); // 추가: 버튼 비활성화 상태 관리
const loginErrorMessage = ref('아이디와 비밀번호를 확인해주세요.');

const handleLogin = async (e: Event) => {
  e.preventDefault(); // 기본 form submit 동작 방지

  // 이메일이나 비밀번호가 비어있는 경우
  if (!email.value || !password.value) {
    showLoginErrorModal.value = true;
    loginErrorMessage.value = '아이디와 비밀번호를 확인해주세요.';
    return;
  }

  try {
    await authStore.login({
      email: email.value,
      password: password.value,
    });

    // redirect 쿼리 파라미터가 있으면 해당 페이지로, 없으면 메인으로 이동
    const redirect = router.currentRoute.value.query.redirect as string;
    await router.push(redirect || '/main');
  } catch (error: any) {
    console.error('Login failed:', error);
    if (error.response?.data?.code === 'UNVALID_PROVIDER') {
      loginErrorMessage.value = '소셜 로그인으로 가입된 계정입니다. 소셜 로그인으로 진행해주세요.';
    } else {
      loginErrorMessage.value = '아이디와 비밀번호를 확인해주세요.';
    }
    showLoginErrorModal.value = true;
  }
};

const handleForgotPassword = () => {
  showResetModal.value = true;
};

const handleSignUp = () => {
  router.push('/signup');
};

const handleResetPassword = async () => {
  if (resetButtonDisabled.value) return;
  resetButtonDisabled.value = true;

  try {
    resetError.value = '';
    if (!resetEmail.value || !resetName.value) {
      resetError.value = '이메일과 이름을 모두 입력해주세요.';
      resetButtonDisabled.value = false;
      return;
    }
    await sendPasswordResetEmail({
      email: resetEmail.value,
      userName: resetName.value,
    });
    showResetModal.value = false;
    showConfirmModal.value = true;
  } catch (error: any) {
    // any 타입으로 지정하여 error.response 접근 가능
    console.error('Password reset failed:', error);

    // 에러 코드에 따른 다른 메시지 표시
    if (error.response?.data?.code === 'UNVALID_PROVIDER') {
      resetError.value = '소셜 로그인으로 가입된 계정입니다. 소셜 로그인으로 진행해주세요.';
    } else if (error.response?.data?.code === 'USERNAME_EMAIL_MISMATCH') {
      resetError.value = '입력하신 이메일과 이름이 일치하지 않습니다. 다시 확인하여 시도해주세요.';
    } else if (error.response?.data?.code === 'FAIL_EMAIL_SEND') {
      resetError.value = '이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.';
    } else {
      resetError.value = '이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.';
    }

    resetButtonDisabled.value = false;
  }
};

const closeModals = () => {
  showResetModal.value = false;
  showConfirmModal.value = false;
  resetEmail.value = '';
  resetName.value = ''; // 추가: 이름 초기화
  resetError.value = '';
  resetButtonDisabled.value = false; // 모달 닫을 때 버튼 상태 초기화
};

const closeLoginErrorModal = () => {
  showLoginErrorModal.value = false;
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

      <!-- 하단: 로그인 폼 영역 -->
      <div class="w-full">
        <h2 class="text-2xl font-bold mb-8">Welcome!</h2>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <BasicInput v-model="email" type="full" placeholder="ID" inputType="text" />
          <BasicInput v-model="password" type="password" placeholder="Password" />

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
            type="submit"
            size="L"
            text="로그인"
            class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
          />
        </form>

        <div class="mt-4">
          <div class="relative flex items-center justify-center">
            <div class="absolute w-full h-[1px] bg-gray-300"></div>
            <span class="relative px-4 bg-white text-sm text-gray-500">또는</span>
          </div>
        </div>

        <div class="mt-4">
          <SocialLoginButtons />
        </div>

        <div class="mt-4 text-center">
          <span class="text-sm text-gray-500">계정이 없으신가요?</span>
          <button
            class="text-sm text-gray-700 font-medium ml-2 hover:text-gray-900"
            @click="handleSignUp"
          >
            회원가입
          </button>
        </div>
      </div>
    </div>

    <!-- 모바일 크기에서만 보이는 기존 컨테이너 -->
    <div class="md:hidden w-full max-w-[384px]">
      <h2 class="text-2xl font-bold mb-8">Welcome!</h2>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <BasicInput v-model="email" type="full" placeholder="ID" inputType="text" />
        <BasicInput v-model="password" type="password" placeholder="Password" />

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
          type="submit"
          size="L"
          text="로그인"
          class="!bg-[#F6F6F3] !text-black hover:!bg-[#DAD7CD] hover:!text-white"
        />

        <div class="mt-4">
          <div class="relative flex items-center justify-center">
            <div class="absolute w-full h-[1px] bg-gray-300"></div>
            <span class="relative px-4 bg-white text-sm text-gray-500">또는</span>
          </div>
        </div>

        <div class="mt-4">
          <SocialLoginButtons />
        </div>

        <div class="mt-4 text-center">
          <span class="text-sm text-gray-500">계정이 없으신가요?</span>
          <button
            class="text-sm text-gray-700 font-medium ml-2 hover:text-gray-900"
            @click="handleSignUp"
          >
            회원가입
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- 비밀번호 재설정 이메일 입력 모달 -->
  <div
    v-if="showResetModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white rounded-lg p-6 w-full max-w-md">
      <h3 class="text-xl font-semibold mb-4">비밀번호 재설정</h3>
      <p class="text-gray-600 mb-4">가입 시 등록한 이메일과 이름을 입력해주세요.</p>

      <div class="space-y-4">
        <BasicInput v-model="resetEmail" type="full" placeholder="이메일" inputType="email" />
        <BasicInput v-model="resetName" type="full" placeholder="이름" inputType="text" />
      </div>

      <p v-if="resetError" class="text-red-500 text-sm mt-2 mb-4">{{ resetError }}</p>

      <div class="flex justify-end gap-2">
        <BasicButton
          size="M"
          text="취소"
          @click="closeModals"
          class="!bg-gray-200 !text-gray-700 hover:!bg-gray-300"
        />
        <BasicButton
          size="M"
          text="전송"
          :disabled="resetButtonDisabled"
          @click="handleResetPassword"
          class="!bg-[#698469] !text-white hover:!bg-[#4a5d4a]"
        />
      </div>
    </div>
  </div>

  <!-- 이메일 전송 확인 모달 -->
  <div
    v-if="showConfirmModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white rounded-lg p-6 w-full max-w-md">
      <h3 class="text-xl font-semibold mb-4">이메일 전송 완료</h3>
      <p class="text-gray-600 mb-4">
        비밀번호 재설정 링크가 이메일로 전송되었습니다. 이메일 함을 확인해주세요.
      </p>

      <div class="flex justify-end">
        <BasicButton
          size="M"
          text="확인"
          @click="closeModals"
          class="!bg-[#698469] !text-white hover:!bg-[#4a5d4a]"
        />
      </div>
    </div>
  </div>

  <!-- 로그인 에러 모달 -->
  <div
    v-if="showLoginErrorModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white rounded-lg p-6 w-full max-w-md">
      <form @submit.prevent="closeLoginErrorModal">
        <p class="text-gray-600 mb-4">{{ loginErrorMessage }}</p>
        <div class="flex justify-end">
          <button
            type="submit"
            class="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition-colors"
          >
            확인
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
