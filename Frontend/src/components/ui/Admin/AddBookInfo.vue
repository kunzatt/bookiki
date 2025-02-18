// components/book/BookInfoPanel.vue
<script setup lang="ts">
import { ref } from 'vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import type { BookInformationResponse } from '@/types/api/bookInformation';
import type { BookItemResponse } from '@/types/api/bookItem';
import { addBookItem } from '@/api/bookItem';

interface Props {
  bookInfo: BookInformationResponse | null;
  isVisible: boolean;
}

const props = defineProps<Props>();
const isLoading = ref(false);
const error = ref<string | null>(null);

// BookItem 생성 이벤트 emit
const emit = defineEmits<{
  (e: 'bookItemCreated', bookItem: BookItemResponse): void;
}>();

// BookItem 생성
const handleCreateBookItem = async () => {
  if (!props.bookInfo) return;

  isLoading.value = true;
  error.value = null;

  try {
    // BookItem 생성 (현재 시간을 purchaseAt으로 사용)
    const bookItem = await addBookItem({
      bookInformationId: props.bookInfo.id,
      purchaseAt: new Date().toISOString(),
    });

    // 부모 컴포넌트에 결과 전달
    emit('bookItemCreated', bookItem);
  } catch (err) {
    error.value = '도서 등록 중 오류가 발생했습니다.';
    console.error('도서 등록 오류:', err);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div v-if="isVisible" class="w-full p-6 bg-white rounded-lg shadow-sm mt-4">
    <div class="flex gap-8">
      <!-- 왼쪽: 도서 이미지 -->
      <div class="w-2/5 max-w-[300px]">
        <img :src="bookInfo?.image" :alt="bookInfo?.title" class="w-full object-contain" />
      </div>

      <!-- 오른쪽: 도서 정보 -->
      <div class="flex-1">
        <h2 class="text-xl font-bold mb-4">{{ bookInfo?.title }}</h2>
        <div class="space-y-3">
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">도서ID</span>
            <span>{{ bookInfo?.id }}</span>
          </div>
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">ISBN</span>
            <span>{{ bookInfo?.isbn }}</span>
          </div>
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">저자</span>
            <span>{{ bookInfo?.author }}</span>
          </div>
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">출판사</span>
            <span>{{ bookInfo?.publisher }}</span>
          </div>
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">출판일</span>
            <span>{{ bookInfo?.publishedAt }}</span>
          </div>
          <div class="flex gap-4">
            <span class="w-24 text-gray-500">카테고리</span>
            <span>{{ bookInfo?.category }}</span>
          </div>
          <div class="flex gap-4 items-start">
            <span class="w-24 text-gray-500">설명</span>
            <p class="flex-1 text-sm text-gray-700">{{ bookInfo?.description }}</p>
          </div>
        </div>

        <!-- 에러 메시지 -->
        <p v-if="error" class="text-red-500 mt-2">{{ error }}</p>

        <!-- 도서 등록 버튼 -->
        <div class="mt-6 flex justify-end">
          <BasicButton text="도서 등록" :disabled="isLoading" @click="handleCreateBookItem" />
        </div>
      </div>
    </div>
  </div>
</template>
