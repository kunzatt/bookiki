<script setup lang="ts">
import { computed } from 'vue';
import type { Pageable } from '@/types/common/pagination';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  sort?: string[];
  visiblePages?: number;
}

const props = withDefaults(defineProps<PaginationProps>(), {
  visiblePages: 7,
  sort: () => []
});

const emit = defineEmits<{
  (e: 'update:pageInfo', value: Pageable): void;
}>();

const pages = computed(() => {
  const pages: number[] = [];
  let startPage = Math.max(1, props.currentPage - Math.floor(props.visiblePages / 2));
  let endPage = Math.min(props.totalPages, startPage + props.visiblePages - 1);
  
  // 마지막 페이지가 총 페이지수를 초과하지 않도록 조정
  if (endPage - startPage + 1 < props.visiblePages) {
    startPage = Math.max(1, endPage - props.visiblePages + 1);
  }
  
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
});

const handlePageClick = (page: number) => {
  if (page !== props.currentPage && page >= 1 && page <= props.totalPages) {
    const pageInfo: Pageable = {
      pageNumber: page - 1, // 백엔드는 0-based index 사용
      pageSize: props.pageSize,
      sort: props.sort
    };
    emit('update:pageInfo', pageInfo);
  }
};
</script>

<template>
  <div class="flex items-center justify-center gap-1">
    <!-- 이전 페이지 버튼 -->
    <button
      class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-100"
      :class="{ 'text-gray-300': currentPage === 1 }"
      :disabled="currentPage === 1"
      @click="handlePageClick(currentPage - 1)"
      aria-label="Previous page"
    >
      <span class="material-icons text-sm">chevron_left</span>
    </button>

    <!-- 페이지 번호 -->
    <button
      v-for="page in pages"
      :key="page"
      class="w-8 h-8 flex items-center justify-center rounded text-sm"
      :class="{
        'bg-[#698469] text-white': page === currentPage,
        'hover:bg-gray-100': page !== currentPage
      }"
      @click="handlePageClick(page)"
      :aria-label="`Go to page ${page}`"
      :aria-current="page === currentPage ? 'page' : undefined"
    >
      {{ page }}
    </button>

    <!-- 다음 페이지 버튼 -->
    <button
      class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-100"
      :class="{ 'text-gray-300': currentPage === totalPages }"
      :disabled="currentPage === totalPages"
      @click="handlePageClick(currentPage + 1)"
      aria-label="Next page"
    >
      <span class="material-icons text-sm">chevron_right</span>
    </button>
  </div>
</template>

<!--
사용 예시:
<template>
  <Pagination
    v-model:pageInfo="pageInfo"
    :current-page="currentPage"
    :total-pages="totalPages"
    :page-size="10"
    :sort="['createdAt,DESC']"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Pageable } from '@/types/common/pagination';

const pageInfo = ref<Pageable>({
  pageNumber: 0,
  pageSize: 10,
  sort: ['createdAt,DESC']
});

const currentPage = ref(1);
const totalPages = ref(10);

// pageInfo가 변경될 때마다 데이터를 새로 불러오는 로직 구현
watch(pageInfo, async (newPageInfo) => {
  // API 호출 및 데이터 업데이트
  // const response = await fetchData(newPageInfo);
  // totalPages.value = response.totalPages;
  // currentPage.value = newPageInfo.pageNumber + 1;
});
</script>
-->