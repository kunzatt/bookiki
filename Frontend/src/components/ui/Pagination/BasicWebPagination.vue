<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  currentPage: number;
  totalPages: number;
  visiblePages?: number;
}

const props = withDefaults(defineProps<Props>(), {
  visiblePages: 7
});

const emit = defineEmits<{
  (e: 'update:currentPage', value: number): void;
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
    emit('update:currentPage', page);
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
    >
      {{ page }}
    </button>

    <!-- 다음 페이지 버튼 -->
    <button
      class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-100"
      :class="{ 'text-gray-300': currentPage === totalPages }"
      :disabled="currentPage === totalPages"
      @click="handlePageClick(currentPage + 1)"
    >
      <span class="material-icons text-sm">chevron_right</span>
    </button>
  </div>
</template>

<!-- 사용예시
<BasicWebPagination
v-model:currentPage="currentPage"
:total-pages="totalPages"
/> -->