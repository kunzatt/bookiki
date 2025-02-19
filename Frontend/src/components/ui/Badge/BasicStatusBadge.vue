<script setup lang="ts">
import type { BadgeStatus, ComponentSize } from '@/types/common/ui';

interface BadgeProps {
  text: string;
  type?: BadgeStatus;
  isEnabled?: boolean;
  size?: ComponentSize;
}

const props = withDefaults(defineProps<BadgeProps>(), {
  type: 'primary',
  isEnabled: true,
  size: 'M',
});

// 사이즈별 Tailwind 클래스 매핑 - 모바일과 데스크톱 구분
const sizeClasses = {
  L: 'w-24 md:w-32 h-8 md:h-10 text-sm md:text-base',
  M: 'w-20 md:w-24 h-6 md:h-8 text-xs md:text-sm',
  S: 'w-16 md:w-20 h-5 md:h-6 text-xs',
} as const;

const getStatusClass = (type: string, isEnabled: boolean, size: ComponentSize) => {
  const styles = {
    // Primary - 기존 메인 컬러 유지 (Chatbot과 동일)
    primary: isEnabled
      ? 'bg-[#698469] text-white'
      : 'bg-[#F8F9F8] text-[#698469] border border-[#698469]',

    // Info - 차분한 파스텔 블루
    info: isEnabled
      ? 'bg-[#6B8EAE] text-white'
      : 'bg-[#F5F8FA] text-[#6B8EAE] border border-[#6B8EAE]',

    // Warning - 부드러운 파스텔 옐로우
    warning: isEnabled
      ? 'bg-[#D4B499] text-white'
      : 'bg-[#FAF6F3] text-[#D4B499] border border-[#D4B499]',

    // Error - 차분한 파스텔 레드
    error: isEnabled
      ? 'bg-[#C1857C] text-white'
      : 'bg-[#F9F3F2] text-[#C1857C] border border-[#C1857C]',

    // Gray - 부드러운 그레이
    gray: isEnabled
      ? 'bg-[#8B8E91] text-white'
      : 'bg-[#F5F6F6] text-[#8B8E91] border border-[#8B8E91]',
  };

  return `${styles[type]} ${sizeClasses[size]} flex items-center justify-center rounded-lg font-medium`;
};
</script>

<template>
  <div :class="getStatusClass(type, isEnabled, size)">
    <slot>{{ text }}</slot>
  </div>
</template>
