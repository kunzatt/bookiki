<!-- src/views/auth/SignUp.vue -->
<script setup lang="ts">
import { ref, computed, watch } from 'vue';
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
const isEmailVerificationAttempted = ref(false);
const isCompanyIdLocked = ref(false);
const verifiedCompanyId = ref(''); // 검증된 사번을 저장

watch(
  () => formData.value.companyId,
  (newValue) => {
    if (isCompanyIdLocked.value) {
      // 잠긴 상태에서 변경을 시도하면 검증된 값으로 되돌림
      formData.value.companyId = verifiedCompanyId.value;
    } else {
      // 잠기지 않은 상태에서는 검증 상태 초기화
      isCompanyIdVerified.value = false;
    }
  },
);

// 비밀번호 정책 검증
const passwordValidation = computed(() => {
  const password = formData.value.password;
  const hasLength = password.length >= 8 && password.length <= 20;
  const hasLetter = /[a-zA-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

  return {
    isValid: hasLength && hasLetter && hasNumber && hasSpecial,
    hasLength,
    hasLetter,
    hasNumber,
    hasSpecial,
  };
});

// 비밀번호 확인 검증 (기존 passwordMismatch를 수정)
const passwordConfirmValidation = computed(() => {
  if (!passwordConfirm.value) return null; // 입력하지 않은 경우
  return formData.value.password === passwordConfirm.value;
});

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

// 이메일 인증 완료 핸들러
const handleEmailVerified = () => {
  isEmailVerified.value = true;
  showToast(AUTH_MESSAGES.SUCCESS.EMAIL_VERIFIED, 'success');
};

// 이메일 인증 실패
const handleEmailVerificationFailed = () => {
  isEmailVerificationAttempted.value = false; // 실패시 재시도 가능하도록 설정
  showToast('이메일 인증에 실패했습니다. 다시 시도해주세요.', 'info');
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
    isCompanyIdLocked.value = true;
    verifiedCompanyId.value = formData.value.companyId; // 검증된 사번 저장
    showToast(AUTH_MESSAGES.SUCCESS.COMPANY_ID_AVAILABLE, 'success');
  } catch (error) {
    isCompanyIdVerified.value = false;
    isCompanyIdLocked.value = false;
    showToast(AUTH_MESSAGES.ERROR.COMPANY_ID_DUPLICATE);
  }
};

// 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    isEmailVerified.value &&
    isCompanyIdVerified.value &&
    formData.value.userName.length > 0 &&
    formData.value.password.length >= 8 &&
    passwordValidation.value.isValid &&
    passwordConfirmValidation.value === true
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
            @failed="handleEmailVerificationFailed"
            :disabled="isEmailVerified"
          />

          <!-- 이름 -->
          <BasicInput v-model="formData.userName" type="full" placeholder="Name" label="이름" />

          <!-- 사번 -->
          <div class="space-y-1">
            <BasicInput
              v-model="formData.companyId"
              type="withButton"
              placeholder="Company ID"
              label="사번"
              button-text="중복 확인"
              :disabled="isCompanyIdLocked"
              :class="{
                'border-red-500': formData.companyId && !isCompanyIdVerified,
                'border-green-500': formData.companyId && isCompanyIdVerified,
              }"
              @button-click="checkCompanyIdDuplicate"
            />
            <!-- 사번 확인 메시지 -->
            <p
              v-if="formData.companyId"
              class="text-xs ml-1"
              :class="{
                'text-red-500': !isCompanyIdVerified,
                'text-green-500': isCompanyIdVerified,
              }"
            >
              {{ isCompanyIdVerified ? '사용 가능한 사번입니다.' : '사번 중복 확인이 필요합니다.' }}
            </p>
          </div>

          <!-- 비밀번호 -->
          <div class="space-y-1">
            <BasicInput
              v-model="formData.password"
              type="password"
              placeholder="Password"
              :class="{
                'border-red-500': formData.password && !passwordValidation.isValid,
                'border-green-500': formData.password && passwordValidation.isValid,
              }"
            />
            <!-- 비밀번호 정책 안내 -->
            <div class="text-xs flex gap-3 ml-1" v-if="formData.password">
              <p
                :class="{
                  'text-green-500': passwordValidation.hasLength,
                  'text-red-500': !passwordValidation.hasLength,
                }"
              >
                • 8~20자 길이
              </p>
              <p
                :class="{
                  'text-green-500': passwordValidation.hasLetter,
                  'text-red-500': !passwordValidation.hasLetter,
                }"
              >
                • 영문 포함
              </p>
              <p
                :class="{
                  'text-green-500': passwordValidation.hasNumber,
                  'text-red-500': !passwordValidation.hasNumber,
                }"
              >
                • 숫자 포함
              </p>
              <p
                :class="{
                  'text-green-500': passwordValidation.hasSpecial,
                  'text-red-500': !passwordValidation.hasSpecial,
                }"
              >
                • 특수문자 포함
              </p>
            </div>
          </div>

          <!-- 비밀번호 확인 -->
          <div class="space-y-1">
            <BasicInput
              v-model="passwordConfirm"
              type="password"
              placeholder="Password 확인"
              :class="{
                'border-red-500': passwordConfirm && !passwordConfirmValidation,
                'border-green-500': passwordConfirm && passwordConfirmValidation,
              }"
            />
            <p
              v-if="passwordConfirm"
              class="text-xs ml-1"
              :class="{
                'text-red-500': !passwordConfirmValidation,
                'text-green-500': passwordConfirmValidation,
              }"
            >
              {{
                passwordConfirmValidation
                  ? '비밀번호가 일치합니다.'
                  : '비밀번호가 일치하지 않습니다.'
              }}
            </p>
          </div>

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
        <EmailVerify
          v-model="formData.email"
          @verified="handleEmailVerified"
          @failed="handleEmailVerificationFailed"
          :disabled="isEmailVerified"
        />

        <!-- 이름 -->
        <BasicInput v-model="formData.userName" type="full" placeholder="Name" label="이름" />

        <!-- 사번 -->
        <div class="space-y-1">
          <BasicInput
            v-model="formData.companyId"
            type="withButton"
            placeholder="Company ID"
            label="사번"
            button-text="중복 확인"
            :disabled="isCompanyIdLocked"
            :class="{
              'border-red-500': formData.companyId && !isCompanyIdVerified,
              'border-green-500': formData.companyId && isCompanyIdVerified,
            }"
            @button-click="checkCompanyIdDuplicate"
          />
          <!-- 사번 확인 메시지 -->
          <p
            v-if="formData.companyId"
            class="text-xs ml-1"
            :class="{
              'text-red-500': !isCompanyIdVerified,
              'text-green-500': isCompanyIdVerified,
            }"
          >
            {{ isCompanyIdVerified ? '사용 가능한 사번입니다.' : '사번 중복 확인이 필요합니다.' }}
          </p>
        </div>

        <!-- 비밀번호 -->
        <div class="space-y-1">
          <BasicInput
            v-model="formData.password"
            type="password"
            placeholder="Password"
            :class="{
              'border-red-500': formData.password && !passwordValidation.isValid,
              'border-green-500': formData.password && passwordValidation.isValid,
            }"
          />
          <!-- 비밀번호 정책 안내 -->
          <div class="text-xs flex gap-3 ml-1" v-if="formData.password">
            <p
              :class="{
                'text-green-500': passwordValidation.hasLength,
                'text-red-500': !passwordValidation.hasLength,
              }"
            >
              • 8~20자 길이
            </p>
            <p
              :class="{
                'text-green-500': passwordValidation.hasLetter,
                'text-red-500': !passwordValidation.hasLetter,
              }"
            >
              • 영문 포함
            </p>
            <p
              :class="{
                'text-green-500': passwordValidation.hasNumber,
                'text-red-500': !passwordValidation.hasNumber,
              }"
            >
              • 숫자 포함
            </p>
            <p
              :class="{
                'text-green-500': passwordValidation.hasSpecial,
                'text-red-500': !passwordValidation.hasSpecial,
              }"
            >
              • 특수문자 포함
            </p>
          </div>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="space-y-1">
          <BasicInput
            v-model="passwordConfirm"
            type="password"
            placeholder="Password"
            :class="{
              'border-red-500': passwordConfirm && !passwordConfirmValidation,
              'border-green-500': passwordConfirm && passwordConfirmValidation,
            }"
          />
          <p
            v-if="passwordConfirm"
            class="text-xs ml-1"
            :class="{
              'text-red-500': !passwordConfirmValidation,
              'text-green-500': passwordConfirmValidation,
            }"
          >
            {{
              passwordConfirmValidation ? '비밀번호가 일치합니다.' : '비밀번호가 일치하지 않습니다.'
            }}
          </p>
        </div>

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
