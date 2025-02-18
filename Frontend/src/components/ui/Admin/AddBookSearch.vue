// components/book/SearchPanel.vue
<script setup lang="ts">
import { ref } from 'vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import { getBookInformationByIsbn } from '@/api/bookInformation';
import type { BookInformationResponse } from '@/types/api/bookInformation';

// ISBN 입력값 상태 관리
const isbn = ref('');
const isLoading = ref(false);

// 검색 결과 emit 정의
const emit = defineEmits<{
  (e: 'searchComplete', bookInfo: BookInformationResponse): void;
  (e: 'searchError', error: Error): void;
}>();

// 검색 버튼 클릭 핸들러
const handleSearch = async () => {
  if (!isbn.value.trim()) return;

  isLoading.value = true;
  try {
    const bookInfo = await getBookInformationByIsbn(isbn.value);
    emit('searchComplete', bookInfo);
  } catch (error) {
    emit('searchError', error as Error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="w-full p-6 bg-white rounded-lg shadow-sm">
    <BasicInput
      type="withButton"
      v-model="isbn"
      placeholder="ISBN을 입력하세요"
      buttonText="입력"
      :disabled="isLoading"
      @button-click="handleSearch"
    />
  </div>
</template>
