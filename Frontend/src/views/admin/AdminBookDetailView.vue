<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { deleteBookItem, getBookAdminDetail } from '@/api/bookItem';
import type { BookAdminDetailResponse } from '@/types/api/bookItem';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';
import BookImage from '@/components/ui/Admin/BookImage.vue';
import BookInfo from '@/components/ui/Admin/BookDetailInfo.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import ConfirmModal from '@/components/ui/Modal/ConfirmModal.vue';
import Toast from '@/components/ui/Alert/ToastAlert.vue';
import BookDetailQRcode from '@/components/ui/Admin/BookDetailQRcode.vue';

const route = useRoute();
const router = useRouter();
const bookDetail = ref<BookAdminDetailResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const showDesktopModal = ref(false);
const showConfirmModal = ref(false);
const showToast = ref(false);
const toastMessage = ref('');

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

// 도서 삭제 핸들러
const handleDeleteClick = () => {
  showConfirmModal.value = true; // 모달을 열기 위한 설정
};

// 도서 삭제 핸들러
const handleDelete = async () => {
  const bookItemId = Number(route.params.id);
  try {
    await deleteBookItem(bookItemId);
    toastMessage.value = '도서가 삭제되었습니다.';
    showToast.value = true;

    // 1초 후 목록 페이지로 이동
    setTimeout(() => {
      router.push('/admin/books');
    }, 1000);
  } catch (error) {
    console.error('도서 삭제 실패:', error);
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
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <!-- 메인 컨텐츠 영역 -->
      <div class="max-w-7xl mx-auto">
        <div v-if="loading" class="text-center">
          <p>데이터를 불러오는 중...</p>
        </div>

        <div v-else-if="error" class="text-red-600">
          <p>데이터를 불러오는데 실패했습니다: {{ error }}</p>
        </div>

        <div v-else-if="bookDetail" class="space-y-6">
          <div class="flex justify-end">
            <BasicButton text="도서 삭제" @click="handleDeleteClick" />
          </div>
          <!-- 삭제 확인 모달 -->
          <ConfirmModal
            v-model="showConfirmModal"
            title="도서 삭제"
            content="정말로 이 도서를 삭제하시겠습니까?"
            icon="warning"
            confirm-text="삭제"
            cancel-text="취소"
            @confirm="handleDelete"
          />
          <!-- 삭제 성공 토스트 -->
          <Toast :is-visible="showToast" :message="toastMessage" type="success" />

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- 이미지 섹션 -->
            <div class="space-y-6 w-full">
              <div class="w-64 sm:w-72 md:w-80 lg:w-96 xl:w-[440px] mx-auto">
                <BookImage
                  :image="bookDetail.image"
                  :title="bookDetail.title"
                  class="h-80 sm:h-96 md:h-[300px] lg:h-[340px] xl:h-[380px]"
                />
                <!-- QR 코드 섹션 -->
                <BookDetailQRcode
                  :qrCode="bookDetail.qrCode"
                  :bookItemId="Number(route.params.id)"
                  class="mt-6"
                />
              </div>
            </div>

            <!-- 도서 정보 섹션 -->
            <BookInfo
              :title="bookDetail.title"
              :author="bookDetail.author"
              :publisher="bookDetail.publisher"
              :isbn="bookDetail.isbn"
              :publishedAt="bookDetail.publishedAt"
              :category="bookDetail.category"
              :bookStatus="bookDetail.bookStatus"
              :purchaseAt="bookDetail.purchaseAt"
              :currentBorrower="bookDetail.currentBorrower"
            />
          </div>
        </div>
      </div>

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
