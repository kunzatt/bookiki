//BasicSelect.vue
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

const sizeClasses = {
  'L': 'w-64 h-9',    // 256px
  'M': 'w-32 h-9',    // 128px
  'S': 'w-24 h-9'     // 96px
} as const;
</script>

<template>
  <div class="relative inline-block">
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
        'pl-2 pr-7 text-sm select-none',
        sizeClasses[size]
      ]"
    >
      <option
        v-for="option in options"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>

    <div class="absolute inset-y-0 right-0 flex items-center pr-1.5 pointer-events-none">
      <span class="material-icons text-gray-400 select-none text-lg">
        expand_more
      </span>
    </div>
  </div>
</template>