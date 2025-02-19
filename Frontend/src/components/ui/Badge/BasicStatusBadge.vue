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
    primary: isEnabled
      ? 'bg-[#698469] text-white'
      : 'bg-[#F8F9F8] text-[#698469] border border-[#698469]',
    info: isEnabled ? 'bg-blue-600 text-white' : 'bg-blue-50 text-blue-600 border border-blue-600',
    warning: isEnabled
      ? 'bg-yellow-600 text-white'
      : 'bg-yellow-50 text-yellow-600 border border-yellow-600',
    error: isEnabled ? 'bg-red-600 text-white' : 'bg-red-50 text-red-600 border border-red-600',
    gray: isEnabled ? 'bg-gray-600 text-white' : 'bg-gray-50 text-gray-600 border border-gray-600',
  };

  return `${styles[type]} ${sizeClasses[size]} flex items-center justify-center rounded-lg font-medium`;
};
</script>

<template>
  <div :class="getStatusClass(type, isEnabled, size)">
    <slot>{{ text }}</slot>
  </div>
</template>
