<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { AxiosError } from 'axios';
import type { BookItemResponse } from '@/types/api/bookItem';
import type { BookInformationResponse } from '@/types/api/bookInformation';
import type { BookBorrowResponse } from '@/types/api/bookHistory';

// API 메서드들 import
import { getBookItemById } from '@/api/bookItem';
import { getBookInformation } from '@/api/bookInformation';
import { checkFavorite, toggleFavorite, getBookFavoriteCount } from '@/api/bookFavorite';
import { borrowBook } from '@/api/bookHistory';

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
const showBorrowModal = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const borrowMessage = ref('');
const isFavorite = ref(false);
const favoriteCount = ref<number>(0);

const $window = ref({
  width: window.innerWidth,
});

const handleResize = () => {
  $window.value.width = window.innerWidth;
};

const handleError = (error: unknown, defaultMessage: string) => {
  if (error instanceof AxiosError) {
    if (error.response?.status === 401) {
      showLoginModal.value = true;
    } else {
      errorMessage.value = defaultMessage;
      showErrorModal.value = true;
    }
  } else {
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

      if (authStore.isAuthenticated) {
        await initializeFavoriteStatus(itemResponse.id);
      }
    }
  } catch (error) {
    handleError(error, '도서 정보를 불러오는데 실패했습니다.');
  }
};

const handleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    showLoginModal.value = true;
    return;
  }

  try {
    if (!bookItem.value?.id) return;

    await toggleFavorite(bookItem.value.id);
    const newStatus = await checkFavorite(bookItem.value.id);
    isFavorite.value = newStatus;
    favoriteCount.value = await getBookFavoriteCount(bookItem.value.id);
  } catch (error: any) {
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
    const status = await checkFavorite(bookItemId);
    isFavorite.value = status;
    favoriteCount.value = await getBookFavoriteCount(bookItemId);
  } catch (error) {
    isFavorite.value = false;
    favoriteCount.value = 0;
  }
};

const handleBorrowClick = async () => {
  if (!authStore.isAuthenticated) {
    showLoginModal.value = true;
    return;
  }

  try {
    if (bookItem.value?.id) {
      if (bookItem.value.bookStatus !== 'AVAILABLE') {
        errorMessage.value = '현재 대출이 불가능한 도서입니다.';
        showErrorModal.value = true;
        return;
      }

      await borrowBook(bookItem.value.id);
      borrowMessage.value = '도서가 성공적으로 대출되었습니다.';
      showBorrowModal.value = true;
      await fetchBookData();
    }
  } catch (error) {
    handleError(error, '도서 대출에 실패했습니다.');
  }
};

const handleErrorConfirm = () => {
  errorMessage.value = '';
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
  if (!authStore.isAuthenticated) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath },
    });
    return;
  }
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
              <!-- Desktop-only borrow button -->
              <div class="mt-8 hidden md:block">
                <BasicButton
                  text="도서 대출하기"
                  size="L"
                  @click="handleBorrowClick"
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
                :type="bookItem?.bookStatus === 'AVAILABLE' ? 'success' : 'error'"
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
              :type="bookItem?.bookStatus === 'AVAILABLE' ? 'success' : 'error'"
              size="M"
              :isEnabled="false"
              class="shrink-0"
            />

            <BasicButton
              text="도서 대출하기"
              size="L"
              @click="handleBorrowClick"
              :isEnabled="bookItem?.bookStatus === 'AVAILABLE'"
            />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 모달들은 동일하게 유지 -->
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

  <!-- Borrow Success Modal -->
  <div
    v-if="showBorrowModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
  >
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">도서 대출</h2>
      <p class="text-gray-600 mb-6">{{ borrowMessage }}</p>
      <div class="flex justify-end">
        <BasicButton text="확인" @click="showBorrowModal = false" />
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  main {
    padding-bottom: 120px;
  }
}
</style>
