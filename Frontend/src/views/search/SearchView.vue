<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import type { SearchType } from '@/types/api/search';
import type { BookItemListResponse } from '@/types/api/bookItem';
import { selectBooks } from '@/api/bookItem';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';

const router = useRouter();
const isLoading = ref(true);
const keyword = ref('');
const searchType = ref<SearchType>('TITLE');
const books = ref<BookItemListResponse[]>([]);
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(0);
const pageInfo = ref({
  pageNumber: 0,
  pageSize: 8,
  sort: ['id,DESC'],
});

const searchTypes = [
  { value: 'TITLE', label: '제목' },
  { value: 'AUTHOR', label: '저자' },
  { value: 'PUBLISHER', label: '출판사' },
];

const handleSearch = async () => {
  try {
    isLoading.value = true;
    const response = await selectBooks(
      pageInfo.value.pageNumber,
      pageInfo.value.pageSize,
      searchType.value,
      keyword.value || undefined,
    );
    books.value = response.content;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('도서 검색 실패:', error);
    error.value = '도서 검색에 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};

watch([keyword, searchType], () => {
  pageInfo.value.pageNumber = 0;
  currentPage.value = 1;
  handleSearch();
});

watch(pageInfo, () => {
  currentPage.value = pageInfo.value.pageNumber + 1;
  handleSearch();
});

onMounted(() => {
  handleSearch();
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="max-w-[1440px] mx-auto">
        <div class="flex flex-col my-6 gap-4">
          <div class="flex gap-4">
            <select v-model="searchType" class="p-2 border rounded-lg flex-shrink-0 w-24">
              <option v-for="type in searchTypes" :key="type.value" :value="type.value">
                {{ type.label }}
              </option>
            </select>

            <div class="relative flex-grow">
              <input
                v-model="keyword"
                type="text"
                :placeholder="`${searchTypes.find((t) => t.value === searchType)?.label}으로 검색`"
                class="w-full p-2 border rounded-lg pr-12"
              />
              <span
                class="material-icons absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400"
              >
                search
              </span>
            </div>
          </div>
        </div>

        <!-- 로딩 상태 -->
        <div
          v-if="isLoading && currentPage === 1"
          class="flex justify-center items-center h-[300px]"
        >
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
            class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-[32px] justify-items-center"
          >
            <div
              v-for="book in books"
              :key="book.id"
              class="book-card w-[140px] sm:w-[165px] md:w-[170px] lg:w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
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

          <!-- 페이지네이션 -->
          <div class="mt-6 flex justify-center">
            <BasicWebPagination
              v-model:pageInfo="pageInfo"
              :current-page="currentPage"
              :total-pages="totalPages"
              :page-size="pageInfo.pageSize"
              :sort="pageInfo.sort"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.material-icons {
  font-family: 'Material Icons';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}
</style>
