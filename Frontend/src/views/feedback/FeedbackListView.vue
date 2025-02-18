<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
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
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <FeedbackList @delete="showUpdateConfirm" />
    </div>
  </div>
</template>
