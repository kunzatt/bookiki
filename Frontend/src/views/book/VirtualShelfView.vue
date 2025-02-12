<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import { getBooksByKeyword } from '@/api/bookItem';
import type { BookItemDisplayResponse } from '@/types/api/bookItem';

const books = ref<BookItemDisplayResponse[]>([]);
const searchKeyword = ref('');
const currentPage = ref(0);
const itemsPerPage = 20;
const loading = ref(false);
const totalPages = ref(0);

const handleSearch = async () => {
  currentPage.value = 0;
  await fetchBooks();
};

const fetchBooks = async () => {
  try {
    loading.value = true;
    const response = await getBooksByKeyword(
      currentPage.value,
      itemsPerPage,
      'id',
      'desc',
      searchKeyword.value
    );
    books.value = response;
  } catch (error) {
    console.error('도서 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

const loadMore = async () => {
  if (loading.value) return;
  currentPage.value++;
  await fetchBooks();
};

onMounted(async () => {
  await fetchBooks();
});
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="mb-8 flex justify-center">
      <BasicInput
        type="withButton"
        v-model="searchKeyword"
        placeholder="도서명을 입력하세요"
        buttonText="검색"
        @buttonClick="handleSearch"
      />
    </div>

    <!-- 책장 배경 -->
    <div class="bg-[#F5E6D3] p-6 rounded-lg">
      <!-- 책장 선반들 -->
      <div class="space-y-8">
        <!-- 각 선반 -->
        <div 
          v-for="shelfIndex in Math.ceil(books.length / 4)" 
          :key="shelfIndex"
          class="relative"
        >
          <!-- 책들 -->
          <div class="grid grid-cols-4 gap-4 px-4 pb-4">
            <RouterLink
              v-for="book in books.slice((shelfIndex - 1) * 4, shelfIndex * 4)"
              :key="book.id"
              :to="{ name: 'BookItemDetail', params: { id: book.id }}"
              class="aspect-[3/4] relative group cursor-pointer"
            >
              <img
                :src="book.image"
                :alt="'Book cover ' + book.id"
                class="w-full h-full object-cover shadow-md transition-transform duration-200 group-hover:scale-105"
              />
            </RouterLink>
          </div>
          <!-- 선반 효과 -->
          <div class="absolute bottom-0 left-0 right-0 h-2 bg-[#8B4513] shadow-md"></div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center my-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-[#698469]"></div>
    </div>

    <div v-if="books.length > 0" class="flex justify-center mt-8">
      <BasicButton
        text="더보기"
        @click="loadMore"
        :disabled="loading"
      />
    </div>

    <div 
      v-if="!loading && books.length === 0" 
      class="flex justify-center items-center h-40 text-gray-500"
    >
      검색 결과가 없습니다.
    </div>
  </div>
</template>

<!-- 
{
  path: '/books/:id',
  name: 'BookItemDetail',
  component: () => import('@/views/BookItemDetailView.vue')
}
-->