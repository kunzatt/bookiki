<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getUserFavorites, toggleFavorite } from '@/api/bookFavorite';
import type { BookFavoriteResponse } from '@/types/api/favorite';
import type { PageResponse } from '@/types/common/pagination';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';

const router = useRouter();
const isLoading = ref(true);
const favorites = ref<BookFavoriteResponse[]>([]);
const error = ref<string | null>(null);
const currentPage = ref(1);
const totalPages = ref(0);
const totalElements = ref(0);
const pageInfo = ref({
  pageNumber: 0,
  pageSize: 8,
  sort: ['createdAt,DESC'],
});
const displayCount = ref(4);
const prevDisplayCount = ref(4);

const updateDisplayCount = () => {
  const containerWidth = window.innerWidth - 48;
  const cardWidth = 175;
  const gap = 32;
  const safetyMargin = 20;
  const maxCards = Math.floor((containerWidth + gap - safetyMargin) / (cardWidth + gap));

  if (window.innerWidth >= 1024) {
    displayCount.value = Math.min(4, maxCards);
  } else if (window.innerWidth >= 768) {
    displayCount.value = Math.min(3, maxCards);
  } else if (window.innerWidth >= 640) {
    displayCount.value = Math.min(2, maxCards);
  } else {
    displayCount.value = 1;
  }

  if (prevDisplayCount.value !== displayCount.value) {
    pageInfo.value = {
      ...pageInfo.value,
      pageSize: displayCount.value,
    };
    prevDisplayCount.value = displayCount.value;
  }
};

const fetchFavorites = async () => {
  try {
    isLoading.value = true;
    const response = await getUserFavorites(
      pageInfo.value.pageNumber,
      pageInfo.value.pageSize,
      pageInfo.value.sort[0],
    );
    favorites.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (err) {
    console.error('좋아요 목록 조회 실패:', err);
    error.value = '좋아요 목록을 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const handleToggleFavorite = async (bookItemId: number, event: Event) => {
  try {
    event.stopPropagation();
    await toggleFavorite(bookItemId);
    await fetchFavorites();
  } catch (err) {
    console.error('좋아요 토글 실패:', err);
  }
};

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};

const debounce = (fn: Function, delay: number) => {
  let timeoutId: ReturnType<typeof setTimeout>;
  return function (...args: any[]) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn.apply(null, args), delay);
  };
};

const debouncedUpdateDisplayCount = debounce(updateDisplayCount, 250);

watch(pageInfo, async () => {
  currentPage.value = pageInfo.value.pageNumber + 1;
  await fetchFavorites();
});

onMounted(async () => {
  await fetchFavorites();
  updateDisplayCount();
  window.addEventListener('resize', debouncedUpdateDisplayCount);
});

onUnmounted(() => {
  window.removeEventListener('resize', debouncedUpdateDisplayCount);
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="max-w-[1440px] mx-auto">
        <div class="flex justify-end my-6">
          <span class="text-gray-600">총 {{ totalElements }}권</span>
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

        <!-- 좋아요 도서 없음 -->
        <div v-else-if="favorites.length === 0" class="text-center py-8 text-gray-600">
          좋아요 한 도서가 없습니다.
        </div>

        <!-- 좋아요 도서 목록 -->
        <div v-else class="w-full relative">
          <div
            class="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-[32px] justify-items-center"
          >
            <div
              v-for="book in favorites"
              :key="book.id"
              class="book-card w-[160px] sm:w-[165px] md:w-[170px] lg:w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
              @click="router.push(`/books/${book.bookItemId}`)"
            >
              <div class="relative">
                <img
                  :src="book.bookImage"
                  :alt="book.bookTitle"
                  class="w-full h-[200px] sm:h-[205px] md:h-[210px] lg:h-[215px] object-cover rounded-t-lg"
                  @error="handleImageError"
                />
                <button
                  @click.stop="handleToggleFavorite(book.bookItemId)"
                  class="absolute top-2 right-2 bg-white rounded-full p-1 shadow-md hover:bg-gray-50"
                >
                  <span class="material-icons text-red-500">favorite</span>
                </button>
              </div>
              <div class="p-3 sm:p-4">
                <h3 class="font-semibold text-sm sm:text-base mb-1 sm:mb-2 truncate">
                  {{ book.bookTitle }}
                </h3>
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
