<!-- QrCode.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import QRCode from 'qrcode.vue';

interface Props {
  qrCode: string | null;
}

const props = withDefaults(defineProps<Props>(), {
  qrCode: null,
});

const qrValue = computed(() => props.qrCode || '');
const qrSize = ref(200);
</script>

<template>
  <div class="bg-white p-6 rounded-lg shadow-md">
    <h2 class="text-xl font-semibold mb-4">QR 코드</h2>
    <div class="flex justify-center">
      <div v-if="qrValue" class="p-4 bg-white rounded-lg">
        <QRCode
          :value="qrValue"
          :size="qrSize"
          level="H"
          render-as="svg"
          :margin="2"
          class="mx-auto"
        />
      </div>
      <div v-else class="text-gray-500">QR 코드가 없습니다.</div>
    </div>
  </div>
</template>
