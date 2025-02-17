<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import BottomNav from '@/components/common/BottomNav.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { getRecommendations } from '../api/recommendation';
import type { RecommendationResponse } from '@/types/api/recommendation';
import axios from '@/api/axios';
import type { BookRankingResponse } from '@/types/api/bookHistory';
import type { BookItemListResponse } from '@/types/api/bookItem';
import { getBookItemById } from '@/api/bookItem';
import { getBookInformation } from '@/api/bookInformation';

const authStore = useAuthStore();
const router = useRouter();

const displayCount = ref(4);
const prevDisplayCount = ref(4);
const isLoading = ref(true);

const monthlyBooks = ref<BookRankingResponse[]>([]);
const recommendedBooks = ref<(BookItemListResponse & { bookItemId: number; bookInfo?: BookInformationResponse })[]>([]);
const recommendationReason = ref<string>('');

// Monthly Books 데이터 가져오기
const fetchMonthlyBooks = async () => {
  try {
    const response = await axios.get('/api/books/ranking');
    monthlyBooks.value = response.data;
  } catch (error) {
    console.error('Error fetching monthly books:', error);
    monthlyBooks.value = [];
  }
};

// 추천 도서 데이터 가져오기
const fetchRecommendedBooks = async () => {
  try {
    // 디스플레이 카운트에 따라 적절한 수의 책을 요청
    const limit = Math.max(8, displayCount.value * 2); // 최소 8개 또는 현재 표시 수의 2배
    const response = await getRecommendations({ limit });
    console.log('추천 도서 데이터:', response); // 디버깅을 위한 로그 추가
    recommendedBooks.value = response.recommendations || [];
    recommendationReason.value = response.recommendationReason || '';

    // 각 책의 상세 정보와 이미지 URL을 가져옵니다
    await Promise.all(
      recommendedBooks.value.map(async (book) => {
        try {
          const bookItem = await getBookItemById(book.bookItemId);
          const bookInfo = await getBookInformation(bookItem.bookInformationId);
          book.bookInfo = bookInfo; // 전체 책 정보를 저장
          book.image = bookInfo.image;
          book.title = bookInfo.title;
          book.author = bookInfo.author;
        } catch (error) {
          console.error(`책 정보 가져오기 실패 (bookItemId: ${book.bookItemId}):`, error);
        }
      })
    );
  } catch (error) {
    console.error('Error fetching recommended books:', error);
    recommendedBooks.value = [];
    recommendationReason.value = '';
  }
};

// 왼쪽으로 슬라이드
const slideLeft = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

// 오른쪽으로 슬라이드
const slideRight = () => {
  if (currentIndex.value < monthlyBooks.value.length - displayCount.value) {
    currentIndex.value++;
  }
};

// 추천 도서 슬라이드 함수
const slideLeftRecommended = () => {
  if (recommendedIndex.value > 0) {
    recommendedIndex.value--;
  }
};

const slideRightRecommended = () => {
  if (recommendedIndex.value < recommendedBooks.value.length - displayCount.value) {
    recommendedIndex.value++;
  }
};

// 화면 크기에 따라 보여줄 도서 개수 조정
const updateDisplayCount = () => {
  const containerWidth = window.innerWidth - 48; // 좌우 패딩 24px씩 제외
  const cardWidth = 175; // 카드 크기
  const gap = 32; // 고정 간격
  const safetyMargin = 20; // 여유 마진
  const hysteresis = 100; // 전환 시점 조절을 위한 히스테리시스 값
  const earlyTransition = 80; // 4->3 전환을 더 빠르게 하기 위한 추가 마진

  // 한 줄에 들어갈 수 있는 최대 카드 수 계산
  let maxCards;
  if (prevDisplayCount.value === 4) {
    // 4개에서 3개로 전환: 더 일찍 전환 (더 큰 마진)
    maxCards = Math.floor(
      (containerWidth + gap - safetyMargin - hysteresis - earlyTransition) / (cardWidth + gap),
    );
  } else {
    // 3개에서 4개로 전환: 더 늦게 전환 (더 작은 마진)
    maxCards = Math.floor((containerWidth + gap - safetyMargin + hysteresis) / (cardWidth + gap));
  }

  // 화면 크기별로 적절한 카드 수 결정
  let newDisplayCount;
  if (window.innerWidth >= 1024) {
    // lg
    newDisplayCount = Math.min(4, maxCards);
  } else if (window.innerWidth >= 768) {
    // md
    newDisplayCount = Math.min(3, maxCards);
  } else if (window.innerWidth >= 640) {
    // sm
    newDisplayCount = Math.min(2, maxCards);
  } else {
    // xs
    newDisplayCount = 1;
  }

  // 이전 상태 저장 후 새로운 값 설정
  prevDisplayCount.value = displayCount.value;
  displayCount.value = newDisplayCount;
};

// 리사이즈 이벤트 디바운스 처리
const debounce = (fn: Function, delay: number) => {
  let timeoutId: ReturnType<typeof setTimeout>;
  return function (...args: any[]) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn.apply(null, args), delay);
  };
};

const debouncedUpdateDisplayCount = debounce(updateDisplayCount, 250);

onMounted(async () => {
  isLoading.value = true;
  try {
    await Promise.all([fetchMonthlyBooks(), fetchRecommendedBooks()]);
  } finally {
    isLoading.value = false;
  }

  updateDisplayCount();
  window.addEventListener('resize', debouncedUpdateDisplayCount);
});

onUnmounted(() => {
  window.removeEventListener('resize', debouncedUpdateDisplayCount);
});

// 이미지 로드 실패 시 기본 이미지로 대체
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};

const currentIndex = ref(0);
const recommendedIndex = ref(0);
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <!-- 데스크톱 사이드바 - lg 이상에서만 표시 -->
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 반응형 헤더 -->
      <HeaderMobile class="lg:hidden" />
      <HeaderDesktop class="hidden lg:block" />

      <main class="flex-1 px-5 lg:px-8 pb-16 lg:pb-8 overflow-y-auto lg:overflow-hidden">
        <div class="max-w-[1440px] mx-auto">
          <div class="h-full lg:flex lg:flex-col lg:gap-8">
            <!-- 이달의 도서 섹션 -->
            <section class="mb-8 lg:mb-0 lg:h-[calc(50%-2rem)]">
              <div class="flex justify-between items-center mb-6">
                <h2 class="text-xl lg:text-2xl font-medium">이달의 도서</h2>
              </div>

              <!-- 로딩 상태 표시 -->
              <div v-if="isLoading" class="flex justify-center items-center h-[300px]">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
              </div>

              <div v-else class="w-full relative flex items-center justify-center px-8">
                <!-- 왼쪽 화살표 -->
                <button
                  @click="slideLeft"
                  class="absolute left-2 z-10 bg-white rounded-full shadow-lg p-2 hover:bg-gray-50 transition-all"
                  :class="{ 'opacity-50 cursor-not-allowed': currentIndex === 0 }"
                  :disabled="currentIndex === 0"
                >
                  <span class="material-icons">chevron_left</span>
                </button>

                <!-- 책 슬라이더 -->
                <div class="w-[calc(100%-4rem)] overflow-hidden py-4">
                  <div
                    class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-[32px] justify-items-center"
                  >
                    <template v-for="index in displayCount" :key="index">
                      <div
                        v-if="monthlyBooks[currentIndex + index - 1]?.bookItemId"
                        class="book-card w-[160px] sm:w-[165px] md:w-[170px] lg:w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                        @click="
                          monthlyBooks[currentIndex + index - 1]?.bookItemId &&
                          router.push(`/books/${monthlyBooks[currentIndex + index - 1].bookItemId}`)
                        "
                      >
                        <div class="relative">
                          <img
                            :src="monthlyBooks[currentIndex + index - 1].image"
                            :alt="monthlyBooks[currentIndex + index - 1].title"
                            class="w-full h-[200px] sm:h-[205px] md:h-[210px] lg:h-[215px] object-cover rounded-t-lg"
                            @error="handleImageError"
                          />
                        </div>
                        <div class="p-3 sm:p-4">
                          <h3 class="font-semibold text-sm sm:text-base mb-1 sm:mb-2 truncate">
                            {{ monthlyBooks[currentIndex + index - 1].title }}
                          </h3>
                          <p class="text-xs sm:text-sm text-gray-600 truncate">
                            {{ monthlyBooks[currentIndex + index - 1].author }}
                          </p>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>

                <!-- 오른쪽 화살표 -->
                <button
                  @click="slideRight"
                  class="absolute right-2 z-10 bg-white rounded-full shadow-lg p-2 hover:bg-gray-50 transition-all"
                  :class="{
                    'opacity-50 cursor-not-allowed':
                      currentIndex >= monthlyBooks.length - displayCount,
                  }"
                  :disabled="currentIndex >= monthlyBooks.length - displayCount"
                >
                  <span class="material-icons">chevron_right</span>
                </button>
              </div>
            </section>

            <!-- 추천 도서 섹션 -->
            <section class="mb-8 lg:mb-0 lg:h-[calc(50%-2rem)]">
              <div class="flex justify-between items-center mb-6">
                <h2 class="text-xl lg:text-2xl font-medium">추천 도서</h2>
              </div>

              <!-- 로딩 상태 표시 -->
              <div v-if="isLoading" class="flex justify-center items-center h-[300px]">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
              </div>

              <div v-else class="w-full relative flex items-center justify-center px-8">
                <!-- 왼쪽 화살표 -->
                <button
                  @click="slideLeftRecommended"
                  class="absolute left-2 z-10 bg-white rounded-full shadow-lg p-2 hover:bg-gray-50 transition-all"
                  :class="{ 'opacity-50 cursor-not-allowed': recommendedIndex === 0 }"
                  :disabled="recommendedIndex === 0"
                >
                  <span class="material-icons">chevron_left</span>
                </button>

                <!-- 책 슬라이더 -->
                <div class="w-[calc(100%-4rem)] overflow-hidden py-4">
                  <div
                    class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-[32px] justify-items-center"
                  >
                    <template v-for="index in displayCount" :key="index">
                      <div
                        v-if="recommendedBooks[recommendedIndex + index - 1]?.bookItemId"
                        class="book-card w-[160px] sm:w-[165px] md:w-[170px] lg:w-[175px] bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                        @click="router.push(`/books/${recommendedBooks[recommendedIndex + index - 1].bookItemId}`)"
                      >
                        <div class="relative">
                          <img
                            :src="recommendedBooks[recommendedIndex + index - 1].bookInfo?.image || '/default-book-cover.svg'"
                            :alt="recommendedBooks[recommendedIndex + index - 1].bookInfo?.title"
                            class="w-full h-[200px] sm:h-[205px] md:h-[210px] lg:h-[215px] object-cover rounded-t-lg"
                            @error="handleImageError"
                          />
                          <!-- 신규 도서 뱃지 -->
                          <div
                            v-if="recommendedBooks[recommendedIndex + index - 1].isNew"
                            class="absolute top-2 right-2 bg-blue-500 text-white text-xs px-2 py-1 rounded"
                          >
                            NEW
                          </div>
                        </div>
                        <div class="p-3 sm:p-4">
                          <h3 class="font-semibold text-sm sm:text-base mb-1 sm:mb-2 truncate">
                            {{ recommendedBooks[recommendedIndex + index - 1].bookInfo?.title }}
                          </h3>
                          <p class="text-xs sm:text-sm text-gray-600 truncate">
                            {{ recommendedBooks[recommendedIndex + index - 1].bookInfo?.author }}
                          </p>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>

                <!-- 오른쪽 화살표 -->
                <button
                  @click="slideRightRecommended"
                  class="absolute right-2 z-10 bg-white rounded-full shadow-lg p-2 hover:bg-gray-50 transition-all"
                  :class="{
                    'opacity-50 cursor-not-allowed':
                      recommendedIndex >= recommendedBooks.length - displayCount,
                  }"
                  :disabled="recommendedIndex >= recommendedBooks.length - displayCount"
                >
                  <span class="material-icons">chevron_right</span>
                </button>
              </div>
            </section>
          </div>
        </div>
      </main>

      <!-- 모바일에서만 BottomNav 표시 -->
      <div class="lg:hidden">
        <BottomNav />
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-card {
  @apply bg-white rounded-lg overflow-hidden shadow hover:shadow-md transition-shadow;
}

/* 반응형 스타일링 */
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

.container {
  scroll-behavior: smooth;
}

.flex {
  will-change: transform;
}
</style>
