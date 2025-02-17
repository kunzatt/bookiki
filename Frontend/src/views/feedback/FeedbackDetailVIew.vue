<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { onMounted } from 'vue';
import FeedbackDetail from '@/components/ui/Detail/FeedbackDetail.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import BottomNav from '@/components/common/BottomNav.vue';

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
  <div class="min-h-screen flex">
    <Sidebar class="hidden md:block fixed h-full" />
    <div class="flex-1 flex flex-col md:ml-64">
      <HeaderMobile class="md:hidden fixed top-0 left-0 right-0 z-10" title="피드백" type="main" />
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="피드백" />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <FeedbackDetail v-if="!isNaN(feedbackId)" :feedback-id="feedbackId" />
        </div>
      </div>
      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>
