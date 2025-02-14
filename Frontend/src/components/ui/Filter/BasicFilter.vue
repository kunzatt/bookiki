<script setup lang="ts">
import { ref, computed } from 'vue';
import BaseDatePicker from '../DatePicker/BaseDatePicker.vue';
import BasicInput from '../Input/BasicInput.vue';
import type { FilterConfig } from '@/types/common/filter';

interface FilterOption {
  label: string;
  value: string | number;
}

interface FilterProps {
  filters: FilterConfig[];
  modelValue: Record<string, any>;
}

const props = defineProps<FilterProps>();
const emit = defineEmits(['update:modelValue', 'apply']);

const localFilters = ref({ ...props.modelValue });

const updateFilter = (key: string, value: any) => {
  localFilters.value[key] = value;
  emit('update:modelValue', { ...localFilters.value });
};

const resetFilters = () => {
  const resetValue: Record<string, any> = {};
  props.filters.forEach((filter) => {
    resetValue[filter.key] = filter.multiple ? [] : '';
  });
  localFilters.value = resetValue;
  emit('update:modelValue', resetValue);
};

const hasActiveFilters = computed(() => {
  return Object.values(localFilters.value).some((value) => {
    if (Array.isArray(value)) {
      return value.length > 0;
    }
    return value !== null && value !== undefined && value !== '';
  });
});

// 검색 필터만 있는지 확인
const isSearchOnly = computed(() => {
  return props.filters.length === 1 && props.filters[0].type === 'search';
});
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow-sm">
    <div class="flex flex-wrap gap-6">
      <!-- 각 필터 요소 -->
      <div v-for="filter in filters" :key="filter.key" class="flex flex-col gap-2">
        <label class="text-sm font-medium text-gray-700">{{ filter.label }}</label>

        <!-- 검색 필터 -->
        <BasicInput
          v-if="filter.type === 'search'"
          type="withButton"
          v-model="localFilters[filter.key]"
          :placeholder="filter.placeholder || '검색어를 입력하세요'"
          buttonText="검색"
          @button-click="$emit('apply')"
          @update:modelValue="
            (value) => {
              updateFilter(filter.key, value);
              $emit('apply'); // 값이 변경될 때마다 바로 적용
            }
          "
        />

        <!-- Select 필터 -->
        <select
          v-else-if="filter.type === 'select'"
          v-model="localFilters[filter.key]"
          class="h-10 px-3 rounded border border-gray-300 focus:border-[#698469] focus:ring focus:ring-[#698469] focus:ring-opacity-50"
          :multiple="filter.multiple"
          @change="updateFilter(filter.key, localFilters[filter.key])"
        >
          <option value="">전체</option>
          <option v-for="option in filter.options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>

        <!-- Date 필터 -->
        <BaseDatePicker
          v-else-if="filter.type === 'date'"
          v-model="localFilters[filter.key]"
          @change="updateFilter(filter.key, localFilters[filter.key])"
        />

        <!-- Radio 필터 -->
        <div v-else-if="filter.type === 'radio'" class="flex gap-4">
          <label
            v-for="option in filter.options"
            :key="option.value"
            class="flex items-center gap-2"
          >
            <input
              type="radio"
              :name="filter.key"
              :value="option.value"
              v-model="localFilters[filter.key]"
              @change="updateFilter(filter.key, localFilters[filter.key])"
              class="text-[#698469] focus:ring-[#698469]"
            />
            <span class="text-sm">{{ option.label }}</span>
          </label>
        </div>

        <!-- Checkbox 필터 -->
        <div v-else-if="filter.type === 'checkbox'" class="flex flex-wrap gap-4">
          <label
            v-for="option in filter.options"
            :key="option.value"
            class="flex items-center gap-2"
          >
            <input
              type="checkbox"
              :value="option.value"
              v-model="localFilters[filter.key]"
              @change="updateFilter(filter.key, localFilters[filter.key])"
              class="text-[#698469] focus:ring-[#698469]"
            />
            <span class="text-sm">{{ option.label }}</span>
          </label>
        </div>
      </div>
    </div>

    <!-- Filter Actions - 검색 필터만 있을 경우 버튼 숨김 -->
    <div v-if="!isSearchOnly" class="flex justify-end mt-4">
      <button
        v-if="hasActiveFilters"
        @click="resetFilters"
        class="px-4 py-2 text-sm text-[#698469] hover:bg-[#F5F7F5] rounded-lg mr-2"
      >
        초기화
      </button>
      <button
        class="px-4 py-2 text-sm text-white bg-[#698469] rounded-lg hover:bg-[#4F6B4F]"
        @click="$emit('apply')"
      >
        적용하기
      </button>
    </div>
  </div>
</template>
