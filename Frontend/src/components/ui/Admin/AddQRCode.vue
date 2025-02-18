<!-- components/book/QRCodePanel.vue -->
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import type { QrCodeResponse } from '@/types/api/qrCode';
import QRCode from 'qrcode';

interface Props {
  qrValue: string;
  bookItemId: number;
  isVisible: boolean;
}

const props = defineProps<Props>();
const qrImageUrl = ref<string>('');

const generateQRCode = async () => {
  if (!props.qrValue) return;

  try {
    qrImageUrl.value = await QRCode.toDataURL(props.qrValue, {
      width: 200,
      margin: 2,
      errorCorrectionLevel: 'H',
    });
  } catch (err) {
    console.error('QR 코드 생성 오류:', err);
  }
};

watch(() => props.qrValue, generateQRCode, { immediate: true });

// QR 코드 이미지 다운로드
const downloadQRCode = () => {
  if (!qrImageUrl.value) return;

  const link = document.createElement('a');
  link.href = qrImageUrl.value;
  link.download = `qr-code-${props.bookItemId}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 디버깅용 watch 추가
watch(
  () => props.qrCode,
  (newValue) => {
    console.log('QR Code props changed:', newValue);
  },
);
</script>

<template>
  <div
    v-if="isVisible"
    class="w-full p-6 bg-white rounded-lg shadow-sm mt-4 flex flex-col items-center"
  >
    <!-- QR 코드 이미지 -->
    <div class="mb-4">
      <img
        v-if="qrImageUrl"
        :src="qrImageUrl"
        :alt="`QR Code for book ${bookItemId}`"
        class="w-[200px] h-[200px]"
      />
    </div>

    <!-- 도서 ID -->
    <p class="text-gray-500 mb-4">도서ID: {{ bookItemId }}</p>

    <!-- 다운로드 버튼 -->
    <BasicButton text="QR 저장" @click="downloadQRCode" />
  </div>
</template>
