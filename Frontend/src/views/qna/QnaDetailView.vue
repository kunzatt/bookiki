<script setup lang="ts">
import { useRoute } from 'vue-router';
import QnaDetail from '@/components/ui/Detail/QnaDetail.vue';

const route = useRoute();
const qnaId = Number(route.params.id);
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <QnaDetail :qna-id="qnaId" />
    </div>
  </div>
</template>
