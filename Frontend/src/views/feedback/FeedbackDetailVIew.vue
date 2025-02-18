<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { onMounted } from 'vue';
import FeedbackDetail from '@/components/ui/Detail/FeedbackDetail.vue';

const route = useRoute();
const router = useRouter();
const feedbackId = Number(route.params.id);

onMounted(() => {
  // feedbackId가 유효한 숫자가 아니면 목록으로 리다이렉트
  if (isNaN(feedbackId)) {
    router.push('/feedbacks');
  }
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <FeedbackDetail v-if="!isNaN(feedbackId)" :feedback-id="feedbackId" />
    </div>
  </div>
</template>
