<script setup lang="ts">
import { formatDate } from '@/types/functions/dateFormats';
import { Category } from '@/types/enums/category';
import { BookStatus } from '@/types/enums/bookStatus';

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
</script>

<template>
  <div class="bg-white rounded-lg shadow p-6">
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
  </div>
</template>
