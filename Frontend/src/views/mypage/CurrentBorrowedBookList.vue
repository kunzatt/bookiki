<script setup lang="ts">
import { ref, onMounted } from 'vue';
import BottomNav from '@/components/common/BottomNav.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import { getCurrentBorrowedBooks } from '@/api/bookHistory';
import { getBookItemById } from '@/api/bookItem';
import { getBookInformation } from '@/api/bookInformation';
import type { BookHistoryResponse } from '@/types/api/bookHistory';
import type { BookItemResponse } from '@/types/api/bookItem';
import type { BookInformationResponse } from '@/types/api/bookInformation';

interface DetailedBookInfo extends BookHistoryResponse {
  bookInformation?: BookInformationResponse;
}

const isLoading = ref(true);
const borrowedBooks = ref<DetailedBookInfo[]>([]);
const error = ref<string | null>(null);

const fetchBookDetails = async () => {
  try {
    isLoading.value = true;
    const borrowedBooksData = await getCurrentBorrowedBooks();

    // 각 대출 도서에 대한 상세 정보 조회
    const detailedBooks = await Promise.all(
      borrowedBooksData.map(async (book) => {
        try {
          const bookItem = await getBookItemById(book.bookItemId);
          const bookInfo = await getBookInformation(bookItem.bookInformationId);
          return { ...book, bookInformation: bookInfo };
        } catch (err) {
          console.error(`도서 정보 조회 실패 (ID: ${book.bookItemId}):`, err);
          return book;
        }
      }),
    );

    borrowedBooks.value = detailedBooks;
  } catch (err) {
    console.error('대출 중인 도서 목록 조회 실패:', err);
    error.value = '도서 목록을 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (date: any) => {
  if (Array.isArray(date)) {
    return new Date(date[0], date[1] - 1, date[2]).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const isOverdue = (overdue: boolean | null) => {
  return overdue === true;
};

onMounted(() => {
  fetchBookDetails();
});
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <HeaderMobile class="lg:hidden" />
      <HeaderDesktop class="hidden lg:block" />

      <main class="flex-1 px-5 lg:px-8 pb-16 lg:pb-8 overflow-y-auto">
        <div class="max-w-[1440px] mx-auto">
          <h1 class="text-2xl font-bold my-6">대출 중인 도서</h1>

          <!-- 로딩 상태 -->
          <div v-if="isLoading" class="flex justify-center items-center h-[300px]">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
          </div>

          <!-- 에러 메시지 -->
          <div v-else-if="error" class="text-center py-8 text-red-600">
            {{ error }}
          </div>

          <!-- 대출 도서 없음 -->
          <div v-else-if="borrowedBooks.length === 0" class="text-center py-8 text-gray-600">
            현재 대출 중인 도서가 없습니다.
          </div>

          <!-- 대출 도서 목록 -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="book in borrowedBooks"
              :key="book.id"
              class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6"
            >
              <div class="flex flex-col md:flex-row gap-6">
                <!-- 도서 이미지 -->
                <div class="flex-shrink-0">
                  <img
                    :src="book.bookInformation?.coverImage || '/default-book-cover.svg'"
                    :alt="book.bookTitle"
                    class="w-full md:w-40 h-56 object-cover rounded-lg"
                    @error="$event.target.src = '/default-book-cover.svg'"
                  />
                </div>

                <!-- 도서 정보 -->
                <div class="flex-1">
                  <h3 class="text-xl font-semibold mb-2">{{ book.bookTitle }}</h3>
                  <p class="text-gray-600 mb-4">{{ book.bookAuthor }}</p>

                  <div class="space-y-2 text-sm">
                    <p v-if="book.bookInformation?.publisher" class="text-gray-600">
                      출판사: {{ book.bookInformation.publisher }}
                    </p>
                    <p v-if="book.bookInformation?.publicationYear" class="text-gray-600">
                      출판년도: {{ book.bookInformation.publicationYear }}
                    </p>
                    <p v-if="book.bookInformation?.isbn" class="text-gray-600">
                      ISBN: {{ book.bookInformation.isbn }}
                    </p>
                  </div>

                  <div class="mt-4 pt-4 border-t border-gray-200">
                    <p class="text-sm mb-2">대출일: {{ formatDate(book.borrowedAt) }}</p>
                    <p :class="['text-sm', { 'text-red-600': isOverdue(book.overdue) }]">
                      반납 예정일: {{ formatDate(book.borrowedAt) }}
                      <span
                        v-if="isOverdue(book.overdue)"
                        class="ml-2 text-xs bg-red-100 text-red-600 px-2 py-1 rounded"
                      >
                        연체
                      </span>
                    </p>
                  </div>
                </div>
              </div>

              <!-- 도서 설명 -->
              <div
                v-if="book.bookInformation?.description"
                class="mt-4 pt-4 border-t border-gray-200"
              >
                <p class="text-sm text-gray-600 line-clamp-3">
                  {{ book.bookInformation.description }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>

      <BottomNav class="lg:hidden" />
    </div>
  </div>
</template>

<style scoped>
.grid {
  scroll-behavior: smooth;
}

@media (min-width: 768px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
