<script setup lang="ts">
import type { BookBorrowResponse } from '@/types/api/bookHistory';

interface BookHistoryProps {
  items: BookBorrowResponse[];
}

const bookhistoryProps = defineProps<BookHistoryProps>();

// 날짜 포맷팅 함수 (YYYY.MM.DD)
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};
</script>

<template>
  <div class="w-full max-w-md mx-auto">
    <ul class="space-y-4">
      <li 
        v-for="(item, index) in items" 
        :key="index"
        class="bg-white p-4 rounded-lg shadow-sm flex justify-between"
      >
        <!-- 왼쪽: 도서 정보 -->
        <div class="flex-1">
          <h3 class="text-base font-medium text-gray-900 mb-1">
            {{ item.bookTitle }}
          </h3>
          <p class="text-sm text-gray-500">
            {{ item.bookAuthor }}
          </p>
        </div>
        
        <!-- 오른쪽: 날짜 정보 -->
        <div class="ml-4 space-y-1 min-w-[140px]">
          <div class="flex justify-between text-sm">
            <span class="text-gray-500">대출일:</span>
            <span class="text-gray-900">{{ formatDate(item.borrowedAt) }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-500">반납일:</span>
            <span :class="item.returnedAt ? 'text-[#FF6B6B]' : 'text-gray-400'">
              {{ item.returnedAt ? formatDate(item.returnedAt) : '-' }}
            </span>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>