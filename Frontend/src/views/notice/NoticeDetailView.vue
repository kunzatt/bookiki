<script setup lang="ts">
import { useRoute } from 'vue-router';
import NoticeDetail from '@/components/ui/Detail/NoticeDetail.vue';

const route = useRoute();
const noticeId = Number(route.params.id);
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <NoticeDetail :notice-id="noticeId" />
    </div>
  </div>
</template>
