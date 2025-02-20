<!-- src/components/ui/Admin/BookDetailQRcode.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import QRCode from 'qrcode';

interface QrCodeInfo {
  id: number;
  qrValue: string;
  createAt: string;
}

interface Props {
  qrCode: QrCodeInfo | null;
  bookItemId: number;
}

const props = defineProps<Props>();
const qrImageUrl = ref<string>('');

const generateQRCode = async () => {
  if (!props.qrCode?.qrValue) return;

  try {
    qrImageUrl.value = await QRCode.toDataURL(props.qrCode.qrValue, {
      width: 200,
      margin: 2,
      errorCorrectionLevel: 'H',
    });
  } catch (err) {
    console.error('QR 코드 생성 오류:', err);
  }
};

watch(() => props.qrCode?.qrValue, generateQRCode, { immediate: true });

const downloadQRCode = () => {
  if (!qrImageUrl.value) return;

  const link = document.createElement('a');
  link.href = qrImageUrl.value;
  link.download = `qr-code-${props.bookItemId}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>

<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h3 class="text-lg font-semibold mb-4">QR 코드 정보</h3>
    <div v-if="props.qrCode" class="flex flex-col items-center">
      <!-- QR 코드 이미지 -->
      <div class="mb-4 flex justify-center">
        <img
          v-if="qrImageUrl"
          :src="qrImageUrl"
          :alt="`QR Code for book ${bookItemId}`"
          class="w-[160px] h-[160px]"
        />
      </div>

      <!-- QR 코드 정보 -->
      <div class="text-sm text-gray-600 mb-4">
        <p>QR ID: {{ props.qrCode.id }}</p>
      </div>

      <!-- 다운로드 버튼 -->
      <BasicButton text="QR 저장" @click="downloadQRCode" />
    </div>
    <div v-else class="text-center text-gray-500">등록된 QR 코드가 없습니다.</div>
  </div>
</template>
