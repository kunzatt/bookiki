<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getBookInformation } from '@/api/bookInformation';
import type { BookInformationResponse } from '@/types/api/bookInformation';
import HeaderMobile from '@/components/common/Header_Mobile.vue';
import BottomNav from '@/components/common/BottomNav.vue';

const route = useRoute();
const bookInfo = ref<BookInformationResponse | null>(null);
const loading = ref(true);

const fetchBookInfo = async () => {
  try {
    loading.value = true;
    const bookId = Number(route.params.id);
    const response = await getBookInformation(bookId);
    bookInfo.value = response;
  } catch (error) {
    console.error('도서 정보 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchBookInfo();
});
</script>

<template>
  <div class="min-h-screen bg-white">
    <!-- 헤더 -->
    <HeaderMobile 
      type="default"
      title="도서 상세 정보"
      :hasNewNotification="false"
    />

    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center items-center h-96">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-[#698469]"></div>
    </div>

    <!-- 도서 정보 -->
    <div v-else-if="bookInfo" class="px-4 pb-16">
      <!-- 도서 기본 정보 -->
      <div class="flex flex-col items-center py-8">
        <img 
          :src="bookInfo.image" 
          :alt="bookInfo.title"
          class="w-48 h-64 object-cover shadow-lg rounded-lg mb-6"
        />

        <!-- 수정 필요 구간 -->
        <!-- 대출 가능 여부 판별 후 위치 확인 버튼 넣어야됨 -> 수정 필요 -->
        <!-- 수정 필요 구간 -->

        <h2 class="text-xl font-bold text-center mb-2">{{ bookInfo.title }}</h2>
        <p class="text-gray-600 mb-1">{{ bookInfo.author }}</p>
        <p class="text-gray-500 text-sm">{{ bookInfo.publisher }}</p>
      </div>

      <!-- 도서 상세 정보 -->
      <div class="space-y-6">
        <section>
          <h3 class="text-lg font-bold mb-2">도서 상세 설명</h3>
          <p class="text-gray-700 leading-relaxed whitespace-pre-line">
            {{ bookInfo.description }}
          </p>
        </section>

        <section>
          <h3 class="text-lg font-bold mb-2">도서 정보</h3>
          <div class="space-y-2">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-gray-600">ISBN</span>
              <span>{{ bookInfo.isbn }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-gray-600">출판일</span>
              <span>{{ new Date(bookInfo.publishedAt).toLocaleDateString() }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-gray-600">카테고리</span>
              <span>{{ bookInfo.category }}</span>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 에러 상태 -->
    <div 
      v-else 
      class="flex justify-center items-center h-96 text-gray-500"
    >
      도서 정보를 불러올 수 없습니다.
    </div>

    <!-- 하단 네비게이션 -->
    <BottomNav />
  </div>
</template>