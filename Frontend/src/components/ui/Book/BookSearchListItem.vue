<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { BookItemListResponse } from '@/types/api/bookItem';

interface Props {
  books: BookItemListResponse[];
  isLoading: boolean;
  isLoadingMore: boolean;
  error: string | null;
  currentPage: number;
}

const props = defineProps<Props>();
const router = useRouter();

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};
</script>

<template>
  <!-- 초기 로딩 상태 -->
  <div v-if="isLoading && currentPage === 1" class="flex justify-center items-center h-[300px]">
    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
  </div>

  <!-- 에러 메시지 -->
  <div v-else-if="error" class="text-center py-8 text-red-600">
    {{ error }}
  </div>

  <!-- 검색 결과 없음 -->
  <div v-else-if="books.length === 0" class="text-center py-8 text-gray-600">
    검색 결과가 없습니다.
  </div>

  <!-- 도서 목록 -->
  <div v-else class="w-full relative">
    <div
      class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 sm:gap-[32px] justify-items-center"
    >
      <div
        v-for="book in books"
        :key="book.id"
        class="book-card w-full max-w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
        @click="router.push(`/books/${book.id}`)"
      >
        <div class="relative">
          <img
            :src="book.image"
            :alt="book.title"
            class="w-full h-[180px] sm:h-[205px] md:h-[210px] lg:h-[215px] object-cover rounded-t-lg"
            @error="handleImageError"
          />
        </div>
        <div class="p-2 sm:p-4">
          <h3 class="font-semibold text-xs sm:text-base mb-1 sm:mb-2 truncate">
            {{ book.title }}
          </h3>
          <p class="text-xs sm:text-sm text-gray-600 truncate">{{ book.author }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
