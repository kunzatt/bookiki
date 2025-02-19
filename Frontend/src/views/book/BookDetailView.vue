<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { AxiosError } from 'axios';
import type { BookItemResponse } from '@/types/api/bookItem';
import type { BookInformationResponse } from '@/types/api/bookInformation';

// API 메서드들 import
import { getBookItemById } from '@/api/bookItem';
import { getBookInformation } from '@/api/bookInformation';
import { checkFavorite, toggleFavorite, getBookFavoriteCount } from '@/api/bookFavorite';

import debounce from 'lodash/debounce';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import BaseError from '@/components/ui/Error/BaseError.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const bookItem = ref<BookItemResponse | null>(null);
const bookInfo = ref<BookInformationResponse | null>(null);
const showLoginModal = ref(false);
const showLocationModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const locationMessage = ref('');
const isFavorite = ref(false);
const favoriteCount = ref<number>(0);

const $window = ref({
  width: window.innerWidth,
});

const handleResize = () => {
  $window.value.width = window.innerWidth;
};

const handleErrorConfirm = () => {
  errorMessage.value = '';
};

// 공통 에러 처리 함수
const handleError = (error: unknown, defaultMessage: string) => {
  if (error instanceof AxiosError) {
    console.error('API Error:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
    });

    if (error.response?.status === 401) {
      showLoginModal.value = true;
    } else {
      errorMessage.value = defaultMessage;
      showErrorModal.value = true;
    }
  } else {
    console.error('Unexpected error:', error);
    errorMessage.value = defaultMessage;
    showErrorModal.value = true;
  }
};

const fetchBookData = async () => {
  try {
    const bookItemId = Number(route.params.id);
    const itemResponse = await getBookItemById(bookItemId);
    bookItem.value = itemResponse;

    if (itemResponse.id) {
      const infoResponse = await getBookInformation(itemResponse.bookInformationId);
      bookInfo.value = infoResponse;

      // 로그인한 경우에만 좋아요 상태 초기화
      if (authStore.isAuthenticated) {
        await initializeFavoriteStatus(itemResponse.id);
      }
    }
  } catch (error) {
    if (error instanceof AxiosError && error.response?.status === 401) {
      // 401 에러는 무시하고 계속 진행
      console.log('Not authenticated, skipping favorite status check');
    } else {
      handleError(error, '도서 정보를 불러오는데 실패했습니다.');
    }
  }
};

const handleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    showLoginModal.value = true;
    return;
  }

  try {
    if (!bookItem.value?.id) {
      return;
    }

    // 서버에 토글 요청
    await toggleFavorite(bookItem.value.id);

    // 서버의 최신 상태 확인
    const newStatus = await checkFavorite(bookItem.value.id);

    // 서버에서 받은 상태로 UI 업데이트
    isFavorite.value = newStatus;

    // 좋아요 수 업데이트
    favoriteCount.value = await getBookFavoriteCount(bookItem.value.id);
  } catch (error: any) {
    // 401 에러 체크를 위해 타입 가드 사용
    if (error?.response?.status === 401) {
      showLoginModal.value = true;
    } else {
      errorMessage.value = '좋아요 처리에 실패했습니다.';
      showErrorModal.value = true;
    }
  }
};

const debouncedHandleFavorite = debounce(
  () => {
    handleFavorite();
  },
  300,
  { leading: true },
);

const initializeFavoriteStatus = async (bookItemId: number) => {
  if (!authStore.isAuthenticated) {
    isFavorite.value = false;
    favoriteCount.value = 0;
    return;
  }

  try {
    console.log('[initializeFavoriteStatus] Checking status for bookId:', bookItemId);
    const status = await checkFavorite(bookItemId);
    console.log('[initializeFavoriteStatus] Initial status:', status);

    isFavorite.value = status;
    favoriteCount.value = await getBookFavoriteCount(bookItemId);
  } catch (error) {
    console.error('[initializeFavoriteStatus] Error:', error);
    isFavorite.value = false;
    favoriteCount.value = 0;
  }
};

const handleLocationClick = async () => {
  if (!authStore.isAuthenticated) {
    showLoginModal.value = true;
    return;
  }

  try {
    if (bookItem.value?.id) {
      if (bookItem.value.bookStatus !== 'AVAILABLE') {
        errorMessage.value = '현재 책장에 없습니다.';
        showErrorModal.value = true;
        return;
      }

      const response = await fetch(`/api/ws/book-location/${bookItem.value.id}`);
      if (!response.ok) {
        throw new Error('API 호출 실패');
      }

      locationMessage.value = '책장을 확인해주세요';
      showLocationModal.value = true;
    }
  } catch (error) {
    console.error('책 위치 조회 실패:', error);
    handleError(error, '책 위치 조회에 실패했습니다.');
  }
};

const goToLogin = () => {
  router.push('/login');
};

const formatPublishDate = (dateStr: string) => {
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}년 ${month}월 ${day}일`;
};

onMounted(async () => {
  window.addEventListener('resize', handleResize);
  await fetchBookData();
});

onUnmounted(() => {
  debouncedHandleFavorite.cancel();
  window.removeEventListener('resize', handleResize);
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div v-if="bookInfo">
        <div class="flex flex-col md:flex-row gap-8 md:gap-20">
          <!-- Book Image Section -->
          <div class="w-[200px] md:w-[400px] mx-auto">
            <div>
              <div class="relative">
                <div class="aspect-[3/4] overflow-hidden rounded-lg shadow-lg">
                  <img
                    :src="bookInfo.image"
                    :alt="bookInfo.title"
                    class="w-full h-full object-cover"
                  />
                </div>
                <!-- Favorite Button -->
                <div class="absolute top-4 right-4">
                  <button
                    @click="debouncedHandleFavorite"
                    class="p-3 bg-white rounded-full shadow-md hover:bg-gray-50 transition-colors duration-200"
                    :class="{ 'text-red-500': isFavorite }"
                  >
                    <i class="material-icons text-2xl">
                      {{ isFavorite ? 'favorite' : 'favorite_border' }}
                    </i>
                  </button>
                </div>
              </div>
              <!-- Desktop-only location button -->
              <div class="mt-8 hidden md:block">
                <BasicButton
                  text="책 위치 찾기"
                  size="L"
                  @click="handleLocationClick"
                  :isEnabled="bookItem?.bookStatus === 'AVAILABLE'"
                />
              </div>
            </div>
          </div>

          <!-- Book Info Section -->
          <div class="flex-1 lg:py-4">
            <div class="lg:w-[600px]">
              <BasicStatusBadge
                v-if="$window.width >= 768"
                :text="bookItem?.bookStatus === 'AVAILABLE' ? '대출 가능' : '대출 불가'"
                :type="bookItem?.bookStatus === 'AVAILABLE' ? success : 'error'"
                size="S"
                :isEnabled="false"
                class="mb-4"
              />

              <div class="flex items-center gap-4 mb-4">
                <h1 class="text-2xl font-bold text-gray-900">{{ bookInfo.title }}</h1>
              </div>

              <div class="flex flex-col gap-2 text-lg text-gray-600 mb-12">
                <span>{{ bookInfo.author }}</span>
                <span>{{ bookInfo.publisher }}</span>
              </div>

              <div class="space-y-12">
                <!-- Book Description -->
                <div>
                  <h2 class="text-2xl font-semibold text-gray-900 mb-4">책 소개</h2>
                  <p class="text-gray-600 leading-relaxed whitespace-pre-line">
                    {{ bookInfo.description }}
                  </p>
                </div>

                <!-- Book Details -->
                <div>
                  <h2 class="text-2xl font-semibold text-gray-900 mb-4">도서 정보</h2>
                  <div class="grid grid-cols-2 gap-y-6 max-w-2xl md:mb-0 mb-24">
                    <div>
                      <span class="text-gray-500 text-sm">저자</span>
                      <p class="text-gray-900 mt-1">{{ bookInfo.author }}</p>
                    </div>
                    <div>
                      <span class="text-gray-500 text-sm">출판사</span>
                      <p class="text-gray-900 mt-1">{{ bookInfo.publisher }}</p>
                    </div>
                    <div>
                      <span class="text-gray-500 text-sm">출판일</span>
                      <p class="text-gray-900 mt-1">
                        {{ formatPublishDate(bookInfo.publishedAt) }}
                      </p>
                    </div>
                    <div>
                      <span class="text-gray-500 text-sm">ISBN</span>
                      <p class="text-gray-900 mt-1">{{ bookInfo.isbn }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Mobile fixed bottom section -->
      <div class="md:hidden fixed bottom-[56px] left-0 right-0 bg-white border-t border-gray-200">
        <div
          class="flex items-center justify-center h-[72px] px-4 mx-auto w-full min-w-[320px] max-w-[1280px]"
        >
          <div class="flex items-center gap-3 w-full max-w-[500px]">
            <BasicStatusBadge
              :text="bookItem?.bookStatus === 'AVAILABLE' ? '대출 가능' : '대출 불가'"
              :type="bookItem?.bookStatus === 'AVAILABLE' ? success : 'error'"
              size="M"
              :isEnabled="false"
              class="shrink-0"
            />
            <BasicButton
              text="책 위치 찾기"
              size="L"
              @click="handleLocationClick"
              :isEnabled="bookItem?.bookStatus === 'AVAILABLE'"
            />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 로그인 모달 -->
  <div
    v-if="showLoginModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
  >
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">로그인이 필요합니다</h2>
      <div class="flex justify-end gap-4">
        <BasicButton text="취소" :isEnabled="false" @click="showLoginModal = false" />
        <BasicButton text="로그인하기" @click="goToLogin" />
      </div>
    </div>
  </div>

  <!-- 에러 모달 -->
  <BaseError v-model="showErrorModal" :content="errorMessage" @confirm="handleErrorConfirm" />

  <!-- Location Modal -->
  <div
    v-if="showLocationModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
  >
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">책 위치 정보</h2>
      <p class="text-gray-600 mb-6">{{ locationMessage }}</p>
      <div class="flex justify-end">
        <BasicButton text="확인" @click="showLocationModal = false" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Add padding to main content to prevent overlap with fixed bottom section on mobile */
@media (max-width: 768px) {
  main {
    padding-bottom: 120px;
  }
}
</style>
