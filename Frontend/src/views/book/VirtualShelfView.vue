<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import { selectBooksByKeyword } from '@/api/bookItem';
import type { BookItemDisplayResponse } from '@/types/api/bookItem';
import type { Pageable } from '@/types/common/pagination';
import DefaultBookCover from '@/assets/images/DEFAULT_BOOK_COVER.png';
import { useRouter } from 'vue-router';
import { AxiosError } from 'axios';
import { useVirtualShelfStore } from '@/stores/virtualShelf';

const virtualShelfStore = useVirtualShelfStore();

const books = ref<BookItemDisplayResponse[]>([]);
const searchKeyword = ref('');
const currentPage = ref(1);
const totalPages = ref(0);
const loading = ref(false);

const pageInfo = ref<Pageable>({
  pageNumber: 0,
  pageSize: 20,
  sort: ['id,DESC'],
});

const fetchBooks = async () => {
  try {
    loading.value = true;

    // 캐시가 유효하고, 페이지 정보와 검색어가 동일한 경우 캐시된 데이터 사용
    if (virtualShelfStore.isBooksCacheValid && 
        virtualShelfStore.booksCache?.pageInfo.pageNumber === pageInfo.value.pageNumber &&
        virtualShelfStore.booksCache?.pageInfo.pageSize === pageInfo.value.pageSize &&
        virtualShelfStore.booksCache?.keyword === (searchKeyword.value || '')) {
      books.value = virtualShelfStore.booksCache.data.content;
      totalPages.value = virtualShelfStore.booksCache.data.totalPages;
      loading.value = false;
      return;
    }

    const response = await selectBooksByKeyword(
      pageInfo.value.pageNumber,
      pageInfo.value.pageSize,
      'id',
      'desc',
      searchKeyword.value || undefined,
    );

    books.value = response.content;
    totalPages.value = response.totalPages;

    // 새로운 데이터를 캐시에 저장
    virtualShelfStore.setBooksCache(
      { content: response.content, totalPages: response.totalPages },
      { ...pageInfo.value },
      searchKeyword.value || ''
    );
  } catch (error) {
    // 404 에러는 검색 결과가 없는 경우이므로 조용히 처리
    if (error instanceof AxiosError && error.response?.status === 404) {
      books.value = [];
      totalPages.value = 0;
      // 캐시 초기화
      virtualShelfStore.clearCache();
    } else {
      console.error('도서 목록 조회 실패:', error);
    }
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  currentPage.value = 1;
  pageInfo.value.pageNumber = 0;
  // 검색어가 변경되면 캐시 초기화
  virtualShelfStore.clearCache();
  await fetchBooks();
};

const getBookCoverImage = (imageUrl: string | null) => {
  return imageUrl || DefaultBookCover;
};

const router = useRouter();

const navigateToBook = (bookId: number) => {
  if (bookId) {
    router.push(`/books/${bookId}`);
  }
};

// books 배열을 행으로 나누는 computed 속성 추가
const bookRows = computed(() => {
  const rows = [];
  for (let i = 0; i < 5; i++) {
    rows.push(books.value.slice(i * 4, (i + 1) * 4));
  }
  return rows;
});

watch(pageInfo, async () => {
  currentPage.value = pageInfo.value.pageNumber + 1;
  await fetchBooks();
});

const handleSearchClick = () => {
  currentPage.value = 1;
  pageInfo.value.pageNumber = 0;
  fetchBooks();
};

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearchClick();
  }
};

onMounted(async () => {
  await fetchBooks();
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="max-w-3xl mx-auto">
        <!-- 검색 바 -->
        <div class="mb-8">
          <BasicInput
            v-model="searchKeyword"
            type="withButton"
            placeholder="도서명을 입력하세요"
            buttonText="검색"
            @button-click="handleSearchClick"
            @keyup="handleKeyPress"
          />
        </div>
        <!-- 책장 -->
        <div class="bg-[#8B4513] p-6 rounded-lg">
          <div class="space-y-8">
            <div
              v-for="(row, rowIndex) in bookRows"
              :key="rowIndex"
              class="relative bg-[#F5E6D3] p-4 rounded"
            >
              <div class="grid grid-cols-4 gap-4">
                <template v-for="colIndex in 4" :key="colIndex">
                  <div v-if="row[colIndex - 1]" class="aspect-[3/4] relative group">
                    <div
                      class="book absolute inset-0 transform transition-transform duration-200 group-hover:scale-105"
                    >
                      <img
                        :src="getBookCoverImage(row[colIndex - 1].image)"
                        :alt="'Book cover ' + row[colIndex - 1].id"
                        class="w-full h-full object-cover rounded shadow-lg cursor-pointer"
                        @click="navigateToBook(row[colIndex - 1].id)"
                      />
                      <div
                        class="absolute top-0 left-0 w-2 h-full bg-black bg-opacity-10 rounded-l"
                      ></div>
                    </div>
                  </div>
                  <div v-else class="aspect-[3/4] relative bg-gray-100 rounded opacity-30"></div>
                </template>
              </div>
              <!-- 선반 효과 -->
              <div
                class="absolute -bottom-4 left-0 right-0 h-4 bg-[#8B4513] shadow-md transform skew-y-1"
              ></div>
              <div
                class="absolute -bottom-4 left-0 right-0 h-2 bg-[#5C2E0E] transform -skew-y-2"
              ></div>
            </div>
          </div>
        </div>
        <!-- 페이지네이션 -->
        <div v-if="books.length > 0" class="mt-8 mb-16 md:mb-8">
          <BasicWebPagination
            v-model:pageInfo="pageInfo"
            :current-page="currentPage"
            :total-pages="totalPages"
            :page-size="pageInfo.pageSize"
            :sort="pageInfo.sort"
          />
        </div>
        <!-- 검색 결과 없음 -->
        <div
          v-if="!loading && books.length === 0"
          class="flex justify-center items-center h-40 text-gray-500"
        >
          검색 결과가 없습니다.
        </div>
      </div>
    </div>
  </div>
  <!-- 로딩 스피너 -->
  <div
    v-if="loading"
    class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
  >
    <div
      class="animate-spin rounded-full h-12 w-12 border-4 border-[#698469] border-t-transparent"
    ></div>
  </div>
</template>

<style scoped>
.book {
  transform-style: preserve-3d;
  box-shadow:
    -6px 6px 8px rgba(0, 0, 0, 0.1),
    -2px 2px 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .flex-1 {
    padding-bottom: 56px;
  }
}
</style>
