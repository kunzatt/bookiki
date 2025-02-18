<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getBookAdminDetail } from '@/api/bookItem';
import type { BookAdminDetailResponse } from '@/types/api/bookItem';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';
import BookImage from '@/components/ui/Admin/BookImage.vue';
import BookInfo from '@/components/ui/Admin/BookDetailInfo.vue';
import QrCode from '@/components/ui/Admin/QrCode.vue';
import type { QrCodeResponse } from '@/types/api/qrCode';

const route = useRoute();
const router = useRouter();
const bookDetail = ref<BookAdminDetailResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const showDesktopModal = ref(false);

const fetchData = async () => {
  try {
    loading.value = true;
    error.value = null;
    const bookItemId = Number(route.params.id);
    bookDetail.value = await getBookAdminDetail(bookItemId);
  } catch (e) {
    error.value = e instanceof Error ? e.message : '알 수 없는 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
};

// 모바일 체크 함수
const isMobileDevice = () => {
  return window.innerWidth < 1024;
};

// 모달 닫기 핸들러
const handleModalClose = () => {
  router.push('/');
};

onMounted(() => {
  if (isMobileDevice()) {
    showDesktopModal.value = true;
  } else {
    fetchData();
  }

  // 리사이즈 이벤트 리스너
  window.addEventListener('resize', () => {
    if (isMobileDevice()) {
      showDesktopModal.value = true;
    }
  });
});

interface Props {
  qrCode: QrCodeResponse | null;
  bookItemId: number;
}
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <HeaderMobile title="도서 상세 정보" class="lg:hidden" />
      <HeaderDesktop title="도서 상세 정보" class="hidden lg:block" />

      <!-- 메인 컨텐츠 영역 -->
      <main class="flex-1 overflow-auto p-6">
        <div class="max-w-7xl mx-auto">
          <div v-if="loading" class="text-center">
            <p>데이터를 불러오는 중...</p>
          </div>

          <div v-else-if="error" class="text-red-600">
            <p>데이터를 불러오는데 실패했습니다: {{ error }}</p>
          </div>

          <div v-else-if="bookDetail" class="space-y-6">
            <!-- 이미지 섹션 -->
            <BookImage :image="bookDetail.image" :title="bookDetail.title" />

            <!-- 도서 정보 섹션 -->
            <BookInfo
              :title="bookDetail.title"
              :author="bookDetail.author"
              :publisher="bookDetail.publisher"
              :isbn="bookDetail.isbn"
              :publishedAt="bookDetail.publishedAt"
              :description="bookDetail.description"
              :category="bookDetail.category"
              :bookStatus="bookDetail.bookStatus"
              :purchaseAt="bookDetail.purchaseAt"
            />

            <!-- QR 코드 섹션 -->
            <QrCode :qr-code="bookDetail.qrCode" :book-item-id="Number(route.params.id)" />
          </div>
        </div>
      </main>

      <div class="lg:hidden">
        <BottomNav />
      </div>
    </div>

    <!-- 데스크탑 전용 알림 모달 -->
    <BaseModal
      v-model="showDesktopModal"
      title="데스크탑 환경에서 확인해주세요"
      content="이 페이지는 데스크탑 환경에서만 이용 가능합니다. PC에서 다시 접속해 주시기 바랍니다."
      icon="desktop_windows"
      confirm-text="메인으로 이동"
      @update:model-value="handleModalClose"
    />
  </div>
</template>
