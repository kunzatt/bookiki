<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import type { SearchType } from '@/types/api/search';
import type { BookItemListResponse } from '@/types/api/bookItem';
import { selectBooks } from '@/api/bookItem';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicSelect from '@/components/ui/Select/BasicSelect.vue';
import BookSearchListItem from '@/components/ui/Book/BookSearchListItem.vue';

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

const handleSearchClick = () => {
  pageInfo.value.pageNumber = 0;
  currentPage.value = 1;
  handleSearch();
};

const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearchClick();
  }
};

watch(pageInfo, () => {
  currentPage.value = pageInfo.value.pageNumber + 1;
  handleSearch();
});

onMounted(() => {
  handleSearch();
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
          :error="error"
          :current-page="currentPage"
        />

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
</template>
