<script setup lang="ts">
import SocialLoginButton from './SocialLoginButton.vue';
</script>

<template>
  <div class="flex flex-col gap-3">
    <SocialLoginButton 
      provider="naver"
      text="네이버로 로그인"
    />
    <SocialLoginButton 
      provider="google"
      text="구글로 로그인"
    />
  </div>
</template>