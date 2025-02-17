<!-- BookList.vue -->
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import BookListItem from './BookListItme.vue';
import { fetchAdminBookList, updateBookStatus } from '@/api/bookItem';
import type { BookAdminListResponse } from '@/types/api/bookItem';
import type { Pageable } from '@/types/common/pagination';
import Pagination from '@/components/ui/Pagination/BasicWebPagination.vue';

interface Props {
  keyword: string;
}

const props = defineProps<Props>();

// 상태 관리
const books = ref<BookAdminListResponse[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// 페이지네이션 상태
const totalElements = ref(0);
const totalPages = ref(1);
const currentPage = ref(1);
const pageSize = ref(10);
const sortBy = ref('id');
const direction = ref('desc');

// 페이지 변경 핸들러
const handlePaginationChange = async (pageInfo: Pageable) => {
  currentPage.value = pageInfo.pageNumber + 1;
  await fetchBooks();
};

const fetchBooks = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await fetchAdminBookList(currentPage.value - 1, pageSize.value, props.keyword);
    books.value = response.content;
    totalElements.value = response.totalElements;
    totalPages.value = response.totalPages;
  } catch (err) {
    error.value = '도서 목록을 불러오는데 실패했습니다.';
    console.error('도서 목록 로딩 실패:', err);
  } finally {
    loading.value = false;
  }
};

const handleStatusUpdate = async (bookId: number, newStatus: string) => {
  try {
    await updateBookStatus(bookId, newStatus);
    await fetchBooks(); // 현재 페이지 새로고침
  } catch (error) {
    console.error('도서 상태 업데이트 실패:', error);
  }
};

// keyword prop이 변경되면 fetchBooks 호출
watch(() => props.keyword, fetchBooks);

onMounted(() => {
  fetchBooks();
});
</script>

<template>
  <div class="w-full">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center items-center py-8">
      <span class="text-gray-500">로딩 중...</span>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="flex justify-center items-center py-8 text-red-500">
      {{ error }}
    </div>

    <!-- 데이터 테이블 -->
    <div v-else class="overflow-x-auto">
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
      <div class="mt-4">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :page-size="pageSize"
          @update:pageInfo="handlePaginationChange"
        />
      </div>
    </div>
  </div>
</template>
