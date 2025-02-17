<script setup lang="ts">
import type { QrCodeResponse } from '@/types/api/qrCode';
import type { ComponentSize } from '@/types/common/ui';

interface QrDisplayProps {
  /**
   * QR 코드 이미지 URL, QR ID
   */
  qrCode: QrCodeResponse;
  /**
   * QR 코드 크기 (선택적)
   * @default 'M'
   */
  size?: ComponentSize;
  /**
   * ID 표시 여부 (선택적)
   * @default true
   */
  showId?: boolean;
}

const props = withDefaults(defineProps<QrDisplayProps>(), {
  size: 'M',
  showId: true
});

const sizeClasses = {
  'L': 'w-32 h-32',  // 128px
  'M': 'w-24 h-24',  // 96px
  'S': 'w-16 h-16'   // 64px
} as const;
</script>

<template>
  <div class="flex flex-col items-center">
    <img 
      :src="qrCode.qrValue" 
      :alt="`QR Code ${qrCode.id}`"
      :class="[
        'rounded border border-gray-200',
        sizeClasses[size]
      ]"
    />
    <span 
      v-if="showId" 
      class="mt-1 text-gray-500"
      :class="{
        'text-sm': size === 'L',
        'text-xs': size === 'M',
        'text-xs': size === 'S'
      }"
    >
      {{ qrCode.id }}
    </span>
  </div>
</template>