<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { getBookByQrCode } from '@/api/bookItem';
import { getBookInformation } from '@/api/bookInformation';
import { getFavoriteStatus, toggleFavorite } from '@/api/favorites';
import type { BookItemResponse } from '@/types/api/bookItem';
import type { BookInformationResponse } from '@/types/api/bookInformation';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';

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

// 윈도우 너비 관리를 위한 ref
const $window = ref({
  width: window.innerWidth
});

// 윈도우 리사이즈 이벤트 핸들러
const handleResize = () => {
  $window.value.width = window.innerWidth;
};

const fetchBookData = async () => {
  try {
    const bookItemId = Number(route.params.id);
    const itemResponse = await getBookByQrCode(bookItemId);
    bookItem.value = itemResponse;
    
    if (itemResponse.bookInformationId) {
      const infoResponse = await getBookInformation(itemResponse.bookInformationId);
      bookInfo.value = infoResponse;
      await checkFavoriteStatus();
    }
  } catch (error) {
    console.error('도서 정보 조회 실패:', error);
    errorMessage.value = '도서 정보를 불러오는데 실패했습니다.';
    showErrorModal.value = true;
  }
};

const handleLocationClick = async () => {
  if (!authStore.isLoggedIn) {
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
      
      // API 호출 및 응답 처리
      const response = await fetch(`/api/ws/book-location/${bookItem.value.id}`);
      if (!response.ok) {
        throw new Error('API 호출 실패');
      }
      
      locationMessage.value = '책장을 확인해주세요';
      showLocationModal.value = true;
    }
  } catch (error) {
    console.error('책 위치 조회 실패:', error);
    errorMessage.value = '책 위치 조회에 실패했습니다.';
    showErrorModal.value = true;
  }
};

// 컴포넌트 마운트 시 이벤트 리스너 등록
onMounted(() => {
  window.addEventListener('resize', handleResize);
  fetchBookData();
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

// 좋아요 상태 확인
const checkFavoriteStatus = async () => {
  try {
    if (bookItem.value?.id) {
      const status = await getFavoriteStatus(bookItem.value.id);
      isFavorite.value = status;
    }
  } catch (error) {
    console.error('좋아요 상태 확인 실패:', error);
  }
};

// 좋아요 토글
const handleFavorite = async () => {
  if (!authStore.isLoggedIn) {
    showLoginModal.value = true;
    return;
  }

  try {
    if (bookItem.value?.id) {
      await toggleFavorite(bookItem.value.id);
      isFavorite.value = !isFavorite.value;
    }
  } catch (error) {
    console.error('좋아요 토글 실패:', error);
    errorMessage.value = '좋아요 처리에 실패했습니다.';
    showErrorModal.value = true;
  }
};

const goToLogin = () => {
  router.push('/login');
};

// 출판일 포맷팅 함수
const formatPublishDate = (dateStr: string) => {
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}년 ${month}월 ${day}일`;
};
</script>

<template>
  <div class="flex min-h-screen bg-gray-100">
    <Sidebar class="hidden lg:block fixed left-0 top-0 h-screen" />
    
    <div class="flex-1 flex flex-col lg:ml-64">
      <HeaderDesktop 
        class="hidden lg:block"
        title="도서 상세"
      />
      <HeaderMobile
        class="lg:hidden"
        title="도서 상세"
        type="default"
      />

      <main class="flex-1 p-6 lg:p-10">
        <div v-if="bookInfo" class="max-w-none mx-auto">
          <div class="flex flex-col lg:flex-row gap-8 lg:gap-20">
            <!-- Book Image Section -->
            <div class="w-full lg:w-[400px]">
              <div>
                <div class="relative">
                  <div class="aspect-[3/4] overflow-hidden rounded-lg shadow-lg">
                    <img 
                      :src="bookInfo.image" 
                      :alt="bookInfo.title"
                      class="w-full h-full object-cover"
                    >
                  </div>
                  <!-- Favorite Button -->
                  <div class="absolute top-4 right-4">
                    <button 
                      @click="handleFavorite"
                      class="p-3 bg-white rounded-full shadow-md hover:bg-gray-50 
                             transition-colors duration-200"
                      :class="{ 'text-red-500': isFavorite }"
                    >
                      <i class="material-icons text-2xl">
                        {{ isFavorite ? 'favorite' : 'favorite_border' }}
                      </i>
                    </button>
                  </div>
                </div>
                <!-- Desktop-only location button -->
                <div class="mt-8 hidden lg:block">
                  <button
                    @click="handleLocationClick"
                    class="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-4 rounded-lg
                          transition-colors duration-200 font-medium text-lg shadow-md"
                  >
                    책 위치 찾기
                  </button>
                </div>
              </div>
            </div>
            
            <!-- Book Info Section -->
            <div class="flex-1 lg:py-4">
              <div class="lg:max-w-3xl">
                <span 
                  v-if="$window.width >= 1024"
                  :class="{
                    'bg-green-500 text-white': bookItem?.bookStatus === 'AVAILABLE',
                    'bg-red-500 text-white': bookItem?.bookStatus !== 'AVAILABLE'
                  }"
                  class="hidden lg:inline-block px-4 py-2 rounded-full text-sm font-medium shadow-sm mb-4"
                >
                  {{ bookItem?.bookStatus === 'AVAILABLE' ? '대출 가능' : '대출 불가' }}
                </span>

                <div class="flex items-center gap-4 mb-4">
                  <h1 class="text-4xl font-bold text-gray-900">{{ bookInfo.title }}</h1>
                </div>
                
                <div class="flex items-center gap-4 text-lg text-gray-600 mb-12">
                  <span>{{ bookInfo.author }}</span>
                  <span class="w-1 h-1 bg-gray-300 rounded-full"></span>
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
                    <div class="grid grid-cols-2 gap-y-6 max-w-2xl">
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
                        <p class="text-gray-900 mt-1">{{ formatPublishDate(bookInfo.publishedAt) }}</p>
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
      </main>
      
      <!-- Mobile fixed bottom section -->
      <div class="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-4 py-3">
        <div class="flex items-center justify-between gap-4 mb-[56px]">
          <span 
            :class="{
              'bg-green-500 text-white': bookItem?.bookStatus === 'AVAILABLE',
              'bg-red-500 text-white': bookItem?.bookStatus !== 'AVAILABLE'
            }"
            class="px-4 py-2 rounded-full text-sm font-medium shadow-sm"
          >
            {{ bookItem?.bookStatus === 'AVAILABLE' ? '대출 가능' : '대출 불가' }}
          </span>
          <button
            @click="handleLocationClick"
            class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg
                   transition-colors duration-200 font-medium shadow-sm"
          >
            책 위치 찾기
          </button>
        </div>
      </div>

      <!-- Bottom Nav only for mobile -->
      <div class="block lg:hidden">
        <BottomNav />
      </div>
    </div>
  </div>

  <!-- 로그인 모달 -->
  <div v-if="showLoginModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">로그인이 필요합니다</h2>
      <div class="flex justify-end gap-4">
        <button
          @click="showLoginModal = false"
          class="px-4 py-2 text-gray-600 hover:text-gray-800"
        >
          취소
        </button>
        <button
          @click="goToLogin"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          로그인하기
        </button>
      </div>
    </div>
  </div>

  <!-- 에러 모달 -->
  <div v-if="showErrorModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">오류</h2>
      <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
      <div class="flex justify-end">
        <button
          @click="showErrorModal = false"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          확인
        </button>
      </div>
    </div>
  </div>

  <!-- Location Modal -->
  <div v-if="showLocationModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
    <div class="bg-white p-6 rounded-lg max-w-sm w-full mx-4">
      <h2 class="text-xl font-bold mb-4">책 위치 정보</h2>
      <p class="text-gray-600 mb-6">{{ locationMessage }}</p>
      <div class="flex justify-end">
        <button
          @click="showLocationModal = false"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          확인
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Add padding to main content to prevent overlap with fixed bottom section on mobile */
@media (max-width: 1024px) {
  main {
    padding-bottom: 120px;
  }
}
</style>
