<script setup lang="ts">
interface Props {
  /**
   * QR 코드 이미지 URL
   */
  imageUrl: string;
  /**
   * QR ID (밑에 표시될 텍스트)
   */
  qrId: string;
  /**
   * QR 코드 크기 (선택적)
   * @default 'M'
   */
  size?: 'L' | 'M' | 'S';
  /**
   * ID 표시 여부 (선택적)
   * @default true
   */
  showId?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
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
      :src="imageUrl" 
      :alt="`QR Code ${qrId}`"
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
      {{ qrId }}
    </span>
  </div>
</template>

<!-- 단일 QR 코드 표시 -->
<!-- 
<QrDisplay 
  image-url="/path/to/qr/image.png"
  qr-id="00000001"
  size="M"
/> 
-->

<!-- QR 코드 나열하기 -->
<!-- 
<div class="flex gap-4 flex-wrap">
  <QrDisplay 
    v-for="qr in qrCodes"
    :key="qr.id"
    :image-url="qr.imageUrl"
    :qr-id="qr.id"
    size="M"
  />
</div> 
-->

<!-- ID 없이 QR 코드만 표시 -->
<!-- 
<QrDisplay 
  image-url="/path/to/qr/image.png"
  qr-id="00000001"
  :show-id="false"
/> 
-->