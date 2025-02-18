<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getCurrentBorrowedBooks } from '@/api/bookHistory';
import { getBookInformation } from '@/api/bookInformation';
import { getBookItemById } from '@/api/bookItem';
import { calculateDueDate } from '@/utils/dateUtils';
import type { BookHistoryResponse } from '@/types/api/bookHistory';

interface BookDetail extends Omit<BookHistoryResponse, 'overdue'> {
  isOverdue: boolean;
  bookInfo?: {
    title: string;
    author: string;
    image: string;
  };
  dueDate?: string;
}

const router = useRouter();
const isLoading = ref(true);
const allBooks = ref<BookDetail[]>([]);
const borrowedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return allBooks.value.slice(start, end);
});
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = ref(8);

const fetchBookDetails = async () => {
  try {
    isLoading.value = true;
    // 1. 현재 대출 중인 도서 목록 조회
    const currentBooks = await getCurrentBorrowedBooks();
    console.log('1. 대출 중인 도서 목록:', currentBooks);

    // 각 도서의 상세 정보를 가져오고 반납 예정일 계산
    const booksWithDetails = await Promise.all(
      currentBooks.map(async (book) => {
        try {
          // 2-1. bookItemId로 도서 아이템 정보 조회
          const bookItem = await getBookItemById(book.bookItemId);
          console.log(`2. 도서 아이템 정보 (ID: ${book.bookItemId}):`, bookItem);

          // 2-2. bookInformationId로 도서 정보 조회
          const bookInfo = await getBookInformation(bookItem.bookInformationId);
          console.log(`3. 도서 상세 정보 (ID: ${bookItem.bookInformationId}):`, bookInfo);

          // 반납 예정일 계산
          const dueDate = await calculateDueDate(book.borrowedAt);

          return {
            ...book,
            isOverdue: book.isOverdue, // overdue를 isOverdue로 변환
            bookInfo: {
              title: bookInfo.title,
              author: bookInfo.author,
              image: bookInfo.image,
            },
            dueDate,
          } as BookDetail;
        } catch (err) {
          console.error(`도서 ID ${book.bookItemId}의 상세 정보 조회 실패:`, err);
          // 기본 도서 정보로 반환
          return {
            ...book,
            isOverdue: book.isOverdue,
            bookInfo: {
              title: book.bookTitle || '제목 없음',
              author: book.bookAuthor || '저자 미상',
              image: '/default-book-cover.svg',
            },
          } as BookDetail;
        }
      }),
    );

    console.log('4. 최종 도서 목록:', booksWithDetails);
    allBooks.value = booksWithDetails;
    totalPages.value = Math.ceil(booksWithDetails.length / pageSize.value);
  } catch (err: any) {
    console.error('대출 중인 도서 목록 조회 실패:', err);
    const errorMessage = err.response?.data?.message || err.message;
    error.value = `도서 목록을 불러오는데 실패했습니다: ${errorMessage}`;
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const isOverdue = (overdue: boolean) => {
  return overdue === true;
};

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};

import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';

onMounted(() => {
  fetchBookDetails();
});

watch(currentPage, () => {
  borrowedBooks;
});

const handlePageInfoUpdate = (pageInfo: any) => {
  currentPage.value = pageInfo.pageNumber + 1;
};
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="max-w-[1440px] mx-auto">
        <div class="flex justify-between items-center my-6">
          <h1 class="text-xl lg:text-2xl font-medium">대출 중인 도서</h1>
          <span class="text-gray-600">총 {{ allBooks.length }}권</span>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="isLoading" class="flex justify-center items-center h-[300px]">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
        </div>

        <!-- 에러 메시지 -->
        <div v-else-if="error" class="text-center py-8 text-red-600">
          {{ error }}
        </div>

        <div v-else>
          <!-- 도서가 없을 때 메시지 -->
          <div v-if="allBooks.length === 0" class="text-center py-8 text-gray-500">
            현재 대출 중인 도서가 없습니다.
          </div>

          <!-- 도서 목록 -->
          <div
            v-else
            class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-[32px] justify-items-center"
          >
            <div
              v-for="book in borrowedBooks"
              :key="book.id"
              class="book-card w-[160px] sm:w-[165px] md:w-[170px] lg:w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
              @click="router.push(`/books/${book.bookItemId}`)"
            >
              <div class="relative">
                <img
                  :src="book.bookInfo?.image || '/default-book-cover.svg'"
                  :alt="book.bookInfo?.title"
                  @error="handleImageError"
                  class="w-full h-[220px] object-cover rounded-t-lg"
                />
                <div
                  v-if="isOverdue(book.isOverdue)"
                  class="absolute top-2 left-2 bg-red-500 text-white text-xs px-2 py-1 rounded"
                >
                  연체
                </div>
              </div>
              <div class="p-3 sm:p-4">
                <h3 class="font-semibold text-sm sm:text-base mb-1 sm:mb-2 truncate">
                  {{ book.bookInfo?.title }}
                </h3>
                <p class="text-xs sm:text-sm text-gray-600 mb-1">
                  {{ book.bookInfo?.author }}
                </p>
                <p class="text-xs text-gray-500">반납 예정일: {{ formatDate(book.dueDate) }}</p>
              </div>
            </div>
          </div>

          <!-- 페이지네이션 -->
          <div v-if="allBooks.length > 0" class="mt-6 flex justify-center">
            <BasicWebPagination
              :current-page="currentPage"
              :total-pages="totalPages"
              :page-size="pageSize"
              :sort="['borrowedAt,DESC']"
              @update:pageInfo="handlePageInfoUpdate"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-card {
  @apply bg-white rounded-lg overflow-hidden shadow hover:shadow-md transition-shadow;
}

@media screen and (min-width: 768px) {
  .grid {
    @apply grid-cols-3;
  }
}

@media screen and (min-width: 1024px) {
  .grid {
    @apply grid-cols-4;
  }
}
</style>
