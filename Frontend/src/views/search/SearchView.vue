[SearchView.vue]
<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import type { SearchType } from '@/types/api/search';
import type { BookItemListResponse } from '@/types/api/bookItem';
import { selectBooks } from '@/api/bookItem';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicSelect from '@/components/ui/Select/BasicSelect.vue';
import BookSearchListItem from '@/components/ui/Book/BookSearchListItem.vue';

const isLoading = ref(true);
const isLoadingMore = ref(false);
const keyword = ref('');
const searchType = ref<SearchType>('TITLE');
const books = ref<BookItemListResponse[]>([]);
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(0);
const isMobile = ref(window.innerWidth < 768);
const hasMore = ref(true);
const observerTarget = ref<HTMLElement | null>(null);

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

const handleSearch = async (isLoadMore: boolean = false) => {
  if (!isLoadMore) {
    isLoading.value = true;
  } else {
    isLoadingMore.value = true;
  }

  try {
    const response = await selectBooks(
      pageInfo.value.pageNumber,
      pageInfo.value.pageSize,
      searchType.value,
      keyword.value || undefined,
    );

    if (isMobile.value && isLoadMore) {
      books.value = [...books.value, ...response.content];
    } else {
      books.value = response.content;
    }

    totalPages.value = response.totalPages;
    hasMore.value = pageInfo.value.pageNumber + 1 < response.totalPages;
  } catch (error) {
    console.error('도서 검색 실패:', error);
    error.value = '도서 검색에 실패했습니다.';
  } finally {
    isLoading.value = false;
    isLoadingMore.value = false;
  }
};

const handleSearchClick = () => {
  pageInfo.value.pageNumber = 0;
  currentPage.value = 1;
  books.value = [];
  handleSearch();
};

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearchClick();
  }
};

const loadMore = async () => {
  if (isLoadingMore.value || !hasMore.value) return;

  pageInfo.value.pageNumber += 1;
  await handleSearch(true);
};

// Intersection Observer 설정
let observer: IntersectionObserver | null = null;

const setupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect();
  }

  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !isLoadingMore.value && hasMore.value && isMobile.value) {
        loadMore();
      }
    },
    { threshold: 0.1 },
  );

  if (observerTarget.value) {
    observer.observe(observerTarget.value);
  }
};

// 반응형 처리
const handleResize = () => {
  const wasMobile = isMobile.value;
  isMobile.value = window.innerWidth < 768;

  if (wasMobile !== isMobile.value) {
    if (isMobile.value) {
      // 데스크톱 -> 모바일 전환
      hasMore.value = pageInfo.value.pageNumber + 1 < totalPages.value;
      setupInfiniteScroll();
    } else {
      // 모바일 -> 데스크톱 전환
      pageInfo.value.pageNumber = 0;
      currentPage.value = 1;
      handleSearch();
    }
  }
};

// 데스크톱 페이지네이션 이벤트 핸들러
watch(pageInfo, () => {
  if (!isMobile.value) {
    currentPage.value = pageInfo.value.pageNumber + 1;
    handleSearch();
  }
});

onMounted(() => {
  handleSearch();
  window.addEventListener('resize', handleResize);
  if (isMobile.value) {
    setupInfiniteScroll();
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (observer) {
    observer.disconnect();
  }
});
</script>

<template>
  <div class="w-full">
    <div class="max-w-7xl mx-auto px-2 sm:px-6 lg:px-8">
      <div class="max-w-[1440px] mx-auto">
        <!-- 필터 영역 -->
        <div class="bg-white p-2 sm:p-4 rounded-lg shadow-sm my-2 sm:my-6">
          <div class="flex flex-col sm:flex-row items-center gap-2 sm:gap-4">
            <div class="w-full sm:w-auto flex flex-col sm:flex-row gap-2 sm:gap-4">
              <div>
                <BasicSelect
                  v-model="searchType"
                  :options="searchTypes"
                  size="M"
                  class="flex justify-start sm:w-auto mb-2 sm:mb-0"
                />
              </div>
              <div class="mb-4">
                <BasicInput
                  v-model="keyword"
                  type="withButton"
                  :placeholder="`${searchTypes.find((t) => t.value === searchType)?.label}으로 검색`"
                  buttonText="검색"
                  @button-click="handleSearchClick"
                  @keyup="handleKeyPress"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 도서 목록 컴포넌트 -->
        <BookSearchListItem
          :books="books"
          :is-loading="isLoading"
          :is-loading-more="isLoadingMore"
          :error="error"
          :current-page="currentPage"
        />

        <!-- 무한 스크롤 로딩 표시 (모바일) -->
        <div v-if="isMobile && hasMore" ref="observerTarget" class="p-4 flex justify-center">
          <div
            v-if="isLoadingMore"
            class="w-6 h-6 border-2 border-gray-900 rounded-full animate-spin border-t-transparent"
          ></div>
        </div>

        <!-- 페이지네이션 (데스크톱) -->
        <div v-if="!isMobile" class="mt-6 flex justify-center">
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
</template>
