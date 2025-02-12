// src/views/MainView.vue
<template>
  <div class="flex h-screen overflow-hidden">
    <!-- 데스크톱 사이드바 - lg 이상에서만 표시 -->
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 반응형 헤더 -->
      <HeaderMobile class="lg:hidden" />
      <HeaderDesktop class="hidden lg:block" />

      <main class="flex-1 px-5 lg:px-8 pb-16 lg:pb-8 overflow-y-auto lg:overflow-hidden">
        <div v-if="authStore.user" class="mb-4 text-sm text-gray-600">
          환영합니다, {{ authStore.user.userName }}님!
        </div>

        <div class="h-full lg:flex lg:flex-col lg:gap-8">
          <!-- 이달의 도서 섹션 -->
          <section class="mb-8 lg:mb-0 lg:h-[calc(50%-2rem)]">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl lg:text-2xl font-medium">이달의 도서</h2>
              <router-link 
                to="/monthly-books" 
                class="text-sm text-[#344E41] hover:text-[#588157] flex items-center"
              >
                더보기
                <span class="material-icons text-sm ml-1">arrow_forward</span>
              </router-link>
            </div>
            <div class="w-full px-2 lg:px-4">
              <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 lg:gap-6">
                <div v-for="book in displayBooks" 
                     :key="book.id"
                     class="book-card w-full max-w-[220px] mx-auto"
                >
                  <div class="relative">
                    <img :src="book.imageUrl" 
                         :alt="book.title"
                         class="w-full h-44 object-cover rounded-t-lg"
                    />
                  </div>
                  <div class="p-4">
                    <h3 class="font-semibold text-base mb-2 truncate">{{ book.title }}</h3>
                    <p class="text-gray-600 text-sm mb-1">{{ book.author }}</p>
                    <p class="text-gray-500 text-sm">{{ book.publisher }}</p>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 추천 도서 섹션 -->
          <section class="mb-8 lg:mb-0 lg:h-[calc(50%-2rem)]">
            <h2 class="text-xl lg:text-2xl font-medium mb-6">추천 도서</h2>
            <div class="grid grid-cols-2 lg:flex lg:gap-6 lg:h-[calc(100%-3rem)]">
              <div v-for="book in recommendedBooks" :key="book.id" class="book-card lg:w-1/5">
                <img 
                  :src="book.image" 
                  :alt="book.title"
                  class="w-full aspect-[3/4] lg:h-[80%] object-cover rounded-lg"
                >
                <div class="p-2 lg:h-[20%] flex flex-col justify-center">
                  <h3 class="text-sm font-medium leading-tight">{{ book.title }}</h3>
                  <p class="text-xs text-gray-500 mt-1">{{ book.author }}</p>
                  <p class="text-xs text-gray-500">{{ book.category }}</p>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>

      <!-- 모바일에서만 BottomNav 표시 -->
      <div class="lg:hidden">
        <BottomNav />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import BottomNav from '@/components/common/BottomNav.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import HeaderMobile from '@/components/common/HeaderMobile.vue'
import HeaderDesktop from '@/components/common/HeaderDesktop.vue'
import { ref, onMounted, computed, onUnmounted } from 'vue'
import axios from 'axios'

const authStore = useAuthStore()

interface Book {
  id: number;
  title: string;
  author: string;
  imageUrl: string;
  isbn: string;
  publisher: string;
  publicationDate: string;
  ranking: number;
  // 백엔드에서 제공하는 다른 필드들도 추가
}

const monthlyBooks = ref<Book[]>([])

const fetchMonthlyBooks = async () => {
  try {
    const response = await axios.get('/api/books/ranking')
    monthlyBooks.value = response.data
  } catch (error) {
    console.error('이달의 도서를 불러오는데 실패했습니다:', error)
  }
}

// 화면 크기에 따라 보여줄 도서 개수 결정
const getDisplayCount = () => {
  if (window.innerWidth >= 1024) return 4;  // lg 이상
  if (window.innerWidth >= 768) return 3;   // md
  return 2;                                 // 모바일
}

// 화면 크기 변경 감지
const displayCount = ref(getDisplayCount());

const updateDisplayCount = () => {
  displayCount.value = getDisplayCount();
}

// 화면 크기 변경 이벤트 리스너
onMounted(() => {
  fetchMonthlyBooks();
  window.addEventListener('resize', updateDisplayCount);
})

onUnmounted(() => {
  window.removeEventListener('resize', updateDisplayCount);
})

// 화면 크기에 따라 도서 개수 조절
const displayBooks = computed(() => {
  return monthlyBooks.value.slice(0, displayCount.value);
});

const recommendedBooks = ref([
  {
    id: 1,
    title: '초보자를 위한 디버깅',
    author: '홍길동',
    category: '기술/공학',
    image: '/images/book1.jpg'
  },
  {
    id: 2,
    title: '디자인 엔지니어링 전략',
    author: '김철수',
    category: '기술/공학',
    image: '/images/book2.jpg'
  }
])
</script>

<style scoped>
.book-card {
  @apply bg-white rounded-lg overflow-hidden shadow hover:shadow-md transition-shadow;
}

/* 반응형 스타일링 */
@media screen and (min-width: 768px) {
  .grid {
    @apply grid-cols-3;
  }
  .book-card {
    @apply max-w-[200px];
  }
}

@media screen and (min-width: 1024px) {
  .grid {
    @apply grid-cols-4;
  }
  .book-card {
    @apply max-w-[180px];
  }
}

@media screen and (max-width: 767px) {
  .grid {
    @apply grid-cols-2;
  }
  .book-card {
    @apply max-w-[160px];
  }
}

.container {
  scroll-behavior: smooth;
}

.flex {
  will-change: transform;
}
</style>