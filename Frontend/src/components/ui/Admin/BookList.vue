<!-- BookList.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import BookListItem from './BookListItme.vue';
import { fetchAdminBookList, updateBookStatus } from '@/api/bookItem';
import type { BookAdminListResponse } from '@/types/api/bookItem';
import type { PageResponse } from '@/types/common/pagination';

const books = ref<BookAdminListResponse[]>([]);
const totalElements = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const keyword = ref('');

const fetchBooks = async (page: number) => {
  try {
    const response = await fetchAdminBookList(page, pageSize.value, keyword.value);
    books.value = response.content;
    totalElements.value = response.totalElements;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('도서 목록 로딩 실패:', error);
  }
};

const handleStatusUpdate = async (bookId: number, newStatus: string) => {
  try {
    await updateBookStatus(bookId, newStatus);
    // 상태 업데이트 후 현재 페이지 새로고침
    await fetchBooks(currentPage.value);
  } catch (error) {
    console.error('도서 상태 업데이트 실패:', error);
  }
};

onMounted(() => {
  fetchBooks(0);
});
</script>

<template>
  <div class="overflow-x-auto">
    <table class="min-w-full">
      <thead class="bg-gray-50">
        <tr>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            ID
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            도서명
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            ISBN
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            카테고리
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            상태
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            QR코드
          </th>
          <th
            class="py-3 px-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
          >
            관리
          </th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <BookListItem
          v-for="book in books"
          :key="book.id"
          :book="book"
          @update-status="handleStatusUpdate"
        />
      </tbody>
    </table>

    <!-- 페이지네이션 -->
    <div class="flex justify-center mt-4 space-x-2">
      <button
        class="px-4 py-2 text-sm border rounded"
        :disabled="currentPage === 0"
        @click="fetchBooks(currentPage - 1)"
      >
        이전
      </button>
      <span class="px-4 py-2 text-sm"> {{ currentPage + 1 }} / {{ totalPages }} </span>
      <button
        class="px-4 py-2 text-sm border rounded"
        :disabled="currentPage === totalPages - 1"
        @click="fetchBooks(currentPage + 1)"
      >
        다음
      </button>
    </div>
  </div>
</template>
