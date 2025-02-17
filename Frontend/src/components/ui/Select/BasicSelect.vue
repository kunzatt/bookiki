<script setup lang="ts">
import type { ComponentSize } from '@/types/common/ui';
import type { SelectOption } from '@/types/common/select';

interface SelectProps {
  size?: ComponentSize;
  disabled?: boolean;
  options: SelectOption[];
  modelValue: string | number;
}

const props = withDefaults(defineProps<SelectProps>(), {
  size: 'M',
  disabled: false,
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void;
}>();

// size별 너비 클래스
const sizeClasses = {
  L: 'w-64',
  M: 'w-32',
  S: 'w-24',
} as const;
</script>

<template>
  <div class="relative inline-block w-full">
    <div class="relative">
      <select
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
        :disabled="disabled"
        :class="[
          'appearance-none rounded border border-gray-300',
          'bg-white text-gray-700',
          'focus:outline-none focus:ring-2 focus:ring-[#698469] focus:border-transparent',
          'hover:border-[#698469]',
          'disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed',
          'pl-3 pr-10 py-2',
          'text-sm',
          'w-full',
          sizeClasses[size],
        ]"
      >
        <option v-for="option in options" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>

      <div
        class="absolute top-0 right-0 h-full px-2 pointer-events-none flex items-center"
        style="transform: translateX(-2px)"
      >
        <span class="material-icons text-gray-400 text-lg"> expand_more </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

select::-ms-expand {
  display: none;
}
</style>
