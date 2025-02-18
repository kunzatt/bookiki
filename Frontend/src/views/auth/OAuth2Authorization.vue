<!-- views/auth/OAuth2Authorization.vue -->
<template>
  <div class="oauth-authorization">
    <p>인증 진행 중...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

onMounted(() => {
  const provider = route.params.provider as string;
  if (!provider) {
    router.push({ name: 'OAuth2Error' });
    return;
  }

  window.location.href = `http://i12a206.p.ssafy.io/oauth2/authorization/${provider}`;
});
</script>
