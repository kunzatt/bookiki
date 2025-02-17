<script setup lang="ts">
import { Category } from '@/enums/category';
import { BookStatus } from '@/enums/bookStatus';

defineProps<{
  title: string;
  author: string;
  publisher: string;
  isbn: string;
  publishedAt: string;
  description: string;
  category: Category;
  bookStatus: BookStatus;
  purchaseAt: string;
}>();

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};
</script>

<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-xl font-semibold mb-4">도서 정보</h2>
    <div class="grid grid-cols-2 gap-4">
      <div>
        <p class="text-gray-600">제목</p>
        <p>{{ title }}</p>
      </div>
      <div>
        <p class="text-gray-600">저자</p>
        <p>{{ author }}</p>
      </div>
      <div>
        <p class="text-gray-600">출판사</p>
        <p>{{ publisher }}</p>
      </div>
      <div>
        <p class="text-gray-600">ISBN</p>
        <p>{{ isbn }}</p>
      </div>
      <div>
        <p class="text-gray-600">출판일</p>
        <p>{{ formatDate(publishedAt) }}</p>
      </div>
      <div>
        <p class="text-gray-600">카테고리</p>
        <p>{{ category }}</p>
      </div>
      <div>
        <p class="text-gray-600">상태</p>
        <p>{{ bookStatus }}</p>
      </div>
      <div>
        <p class="text-gray-600">구매일</p>
        <p>{{ formatDate(purchaseAt) }}</p>
      </div>
    </div>
    <div class="mt-4">
      <p class="text-gray-600">도서 설명</p>
      <p class="mt-2">{{ description }}</p>
    </div>
  </div>
</template>
