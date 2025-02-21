// views/admin/AdminAddBookView.vue
<script setup lang="ts">
import { ref } from 'vue';
import QRCode from 'qrcode';
import SearchPanel from '@/components/ui/Admin/AddBookSearch.vue';
import BookInfoPanel from '@/components/ui/Admin/AddBookInfo.vue';
import QRCodePanel from '@/components/ui/Admin/AddQRCode.vue';
import type { BookInformationResponse } from '@/types/api/bookInformation';
import type { BookItemResponse } from '@/types/api/bookItem';
import { createQRCode } from '@/api/qr';
import type { QrCodeResponse } from '@/types/api/qrCode';

// 상태 관리
const bookInfo = ref<BookInformationResponse | null>(null);
const bookItem = ref<BookItemResponse | null>(null);
const qrCode = ref<QrCodeResponse | null>(null);
const showBookInfo = ref(false);
const showQRCode = ref(false);
const error = ref<string | null>(null);
const isLoading = ref(false);

// ISBN 검색 완료 핸들러
const handleSearchComplete = (info: BookInformationResponse) => {
  bookInfo.value = info;
  showBookInfo.value = true;
  showQRCode.value = false;
  bookItem.value = null;
  qrCode.value = null;
  error.value = null;
};

// 검색 에러 핸들러
const handleSearchError = (err: Error) => {
  bookInfo.value = null;
  showBookInfo.value = false;
  showQRCode.value = false;
  bookItem.value = null;
  qrCode.value = null;
  error.value = '도서 정보를 찾을 수 없습니다. ISBN을 확인해주세요.';
};

// QR 코드 이미지 생성 함수
const generateQRImage = async (value: string): Promise<string> => {
  return await QRCode.toDataURL(value, {
    width: 200,
    margin: 2,
    errorCorrectionLevel: 'H',
  });
};

// 도서 아이템 생성 완료 핸들러
const handleBookItemCreated = async (newBookItem: BookItemResponse) => {
  try {
    isLoading.value = true;
    bookItem.value = newBookItem;

    // QR 코드 생성 (서버 API 호출)
    const qrCodeData = await createQRCode(newBookItem.id);
    bookItem.value = {
      ...newBookItem,
      qrCode: qrCodeData,
    };
    showQRCode.value = true;
  } catch (err) {
    error.value = 'QR 코드 생성 중 오류가 발생했습니다.';
    console.error('QR 코드 생성 오류:', err);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="max-w-5xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">도서 등록</h1>

    <!-- ISBN 검색 패널 -->
    <SearchPanel @search-complete="handleSearchComplete" @search-error="handleSearchError" />

    <!-- 에러 메시지 -->
    <p v-if="error" class="mt-4 text-red-500">{{ error }}</p>

    <!-- 로딩 표시 -->
    <div v-if="isLoading" class="mt-4 text-center">
      <p>처리 중...</p>
    </div>

    <!-- 도서 정보 패널 -->
    <BookInfoPanel
      :book-info="bookInfo"
      :is-visible="showBookInfo"
      :is-loading="isLoading"
      @book-item-created="handleBookItemCreated"
    />

    <!-- QR 코드 패널 -->
    <QRCodePanel
      v-if="bookItem?.qrCode"
      :qr-value="bookItem.qrCode.qrValue"
      :book-item-id="bookItem.id"
      :is-visible="showQRCode"
    />
  </div>
</template>

<style scoped>
.max-w-5xl {
  max-width: 64rem;
}
</style>
