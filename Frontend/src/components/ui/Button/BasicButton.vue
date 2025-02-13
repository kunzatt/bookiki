<script setup lang="ts">
import type { ComponentSize } from '@/types/common/ui';

interface ButtonProps {
  /**
   * 버튼 크기 (L, M, S)
   * @default 'M'
   */
  size?: ComponentSize;
  /**
   * 버튼 활성화 여부
   * @default true
   */
  isEnabled?: boolean;
  /**
   * 버튼 비활성화 여부
   * @default false
   */
  disabled?: boolean;
  /**
   * 버튼 텍스트
   */
  text?: string;
}

const buttonProps = withDefaults(defineProps<ButtonProps>(), {
  size: 'M',
  isEnabled: true,
  disabled: false,
  text: ''
});

defineEmits<{
  (e: 'click'): void;
}>();

// 사이즈별 Tailwind 클래스 매핑
const sizeClasses = {
  'L': 'w-full h-12 text-base',
  'M': 'w-32 h-12 text-base',
  'S': 'w-16 h-12 text-sm'
} as const;
</script>

<template>
  <button
    :class="[
      'rounded-lg font-medium transition-colors duration-200 disabled:opacity-50 px-4',
      sizeClasses[size],
      isEnabled 
        ? 'bg-[#698469] text-white hover:bg-[#4F634F]' 
        : 'bg-[#F8F9F8] text-[#698469] border border-[#698469] hover:bg-[#EAEFEA]'
    ]"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot>{{ text }}</slot>
  </button>
</template>