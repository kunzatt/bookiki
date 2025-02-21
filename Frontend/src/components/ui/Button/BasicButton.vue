<script setup lang="ts">
import type { ComponentSize } from '@/types/common/ui';

interface ButtonProps {
  size?: ComponentSize;
  isEnabled?: boolean;
  disabled?: boolean;
  text?: string;
}

const props = withDefaults(defineProps<ButtonProps>(), {
  size: 'M',
  isEnabled: true,
  disabled: false,
  text: '',
});

defineEmits<{
  (e: 'click'): void;
}>();

// 사이즈별 Tailwind 클래스 매핑
// 높이는 모바일 40px, 데스크톱 48px로 통일
const sizeClasses = {
  L: 'w-full h-10 md:h-12 text-sm md:text-base',
  M: 'w-28 md:w-32 h-10 md:h-12 text-sm md:text-base',
  S: 'w-14 md:w-16 h-10 md:h-12 text-xs md:text-sm',
} as const;
</script>

<template>
  <button
    :class="[
      'rounded-lg font-medium transition-colors duration-200 disabled:opacity-50 px-3 md:px-4',
      sizeClasses[size],
      isEnabled
        ? 'bg-[#698469] text-white hover:bg-[#4F634F]'
        : 'bg-[#F8F9F8] text-[#698469] border border-[#698469] hover:bg-[#EAEFEA]',
    ]"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot>{{ text }}</slot>
  </button>
</template>
