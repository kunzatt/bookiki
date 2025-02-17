<script setup lang="ts">
import type { BadgeStatus } from '@/types/common/ui';

interface BadgeProps {
  /**
   * 배지에 표시될 텍스트
   */
  text: string;
  /**
   * 배지 타입
   * - primary: 주요 상태 (#698469 계열)
   * - info: 정보성 상태
   * - warning: 주의 상태
   * - error: 오류 상태
   * - gray: 기본 회색 상태
   * @default 'primary'
   */
  type?: BadgeStatus;
  /**
   * 배지의 활성화 여부 (outline 스타일)
   * @default true
   */
  isEnabled?: boolean;
}

const props = withDefaults(defineProps<BadgeProps>(), {
  type: 'primary',
  isEnabled: true
});

const getStatusClass = (type: string, isEnabled: boolean) => {
  // 배경색과 텍스트 색상 정의
  const styles = {
    primary: isEnabled
      ? 'bg-[#698469] text-white'
      : 'bg-[#F8F9F8] text-[#698469] border border-[#698469]',
    info: isEnabled
      ? 'bg-blue-600 text-white'
      : 'bg-blue-50 text-blue-600 border border-blue-600',
    warning: isEnabled
      ? 'bg-yellow-600 text-white'
      : 'bg-yellow-50 text-yellow-600 border border-yellow-600',
    error: isEnabled
      ? 'bg-red-600 text-white'
      : 'bg-red-50 text-red-600 border border-red-600',
    gray: isEnabled
      ? 'bg-gray-600 text-white'
      : 'bg-gray-50 text-gray-600 border border-gray-600'
  };
  
  // min-width를 추가하고 w-16을 제거하여 유동적 너비 지원
  return `${styles[type]} min-w-16 h-8 px-3 flex items-center justify-center rounded-lg text-sm font-medium`;
};
</script>

<template>
  <div :class="getStatusClass(type, isEnabled)">
    <slot>{{ text }}</slot>
  </div>
</template>