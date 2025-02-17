<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import { useAuthStore } from '@/stores/auth';
import FeedbackList from '@/components/ui/List/FeedbackList.vue';

const router = useRouter();
const authStore = useAuthStore();

const showUpdateModal = ref(false);
const updateFeedbackId = ref<number | null>(null);

const showUpdateConfirm = (id: number) => {
  updateFeedbackId.value = id;
  showUpdateModal.value = true;
};
</script>

<template>
  <div class="min-h-screen flex">
    <Sidebar class="hidden md:block fixed h-full" />
    <div class="flex-1 flex flex-col md:ml-64">
      <HeaderMobile class="md:hidden fixed top-0 left-0 right-0 z-10" title="피드백" type="main" />
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="피드백" />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <FeedbackList @delete="showUpdateConfirm" />
        </div>
      </div>

      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>
