<script setup lang="ts">
import { useRouter } from 'vue-router';
import naverLogo from '@/assets/images/naver-logo.png';
import googleLogo from '@/assets/images/google-logo.png';

interface SocialLoginButtonProps {
  provider: 'naver' | 'google';
  text: string;
}

const logoImages = {
  naver: naverLogo,
  google: googleLogo,
};

const props = defineProps<SocialLoginButtonProps>();
const router = useRouter();

const handleSocialLogin = (provider: string) => {
  // 백엔드의 OAuth2 인증 엔드포인트로 리다이렉트
  const authUrl = `${import.meta.env.VITE_API_URL}/api/oauth2/authorization/${provider}`;
  window.location.href = authUrl;

  console.log(`${provider} 로그인 시도`);
};
</script>

<template>
  <button
    :class="[
      'w-full h-12 rounded-lg flex items-center justify-center gap-2 transition-colors duration-200',
      provider === 'naver'
        ? 'bg-[#03C75A] hover:bg-[#02B350] text-white'
        : 'bg-white hover:bg-gray-50 text-gray-700 border border-gray-300',
    ]"
    @click.prevent="handleSocialLogin(provider)"
    type="button"
  >
    <img :src="logoImages[provider]" :alt="`${provider} 로고`" class="w-5 h-5" />
    <span class="font-medium">{{ text }}</span>
  </button>
</template>

<script lang="ts"></script>
