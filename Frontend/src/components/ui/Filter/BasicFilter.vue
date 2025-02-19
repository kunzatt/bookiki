//BasicFilter.vue
<script setup lang="ts">
import { ref, computed } from 'vue';
import BaseDatePicker from '../DatePicker/BaseDatePicker.vue';
import BasicInput from '../Input/BasicInput.vue';
import BasicSelect from '../Select/BasicSelect.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { SelectOption } from '@/types/common/select';

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
  emit('apply');
};

const hasActiveFilters = computed(() => {
  return Object.values(localFilters.value).some((value) => {
    if (Array.isArray(value)) {
      return value.length > 0;
    }
    return value !== null && value !== undefined && value !== '';
  });
});

const isSearchOnly = computed(() => {
  return props.filters.length === 1 && props.filters[0].type === 'search';
});
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow-sm">
    <div class="flex flex-col md:flex-row items-start md:items-center gap-4">
      <template v-for="filter in filters" :key="filter.key">
        <div v-if="filter.type === 'search'" class="w-full md:w-[300px]">
          <!-- 기존 w-[400px]에서 수정 -->
          <BasicInput
            type="withButton"
            v-model="localFilters[filter.key]"
            :placeholder="filter.placeholder || '검색어를 입력하세요'"
            buttonText="검색"
            @button-click="$emit('apply')"
            @keyup.enter="$emit('apply')"
            @update:modelValue="(value) => updateFilter(filter.key, value)"
          />
        </div>

        <!-- Select 필터 -->
        <BasicSelect
          v-else-if="filter.type === 'select'"
          v-model="localFilters[filter.key]"
          :options="filter.options"
          :placeholder="filter.label"
          class="md:w-[120px]"
          @update:modelValue="
            (value) => {
              updateFilter(filter.key, value);
              $emit('apply');
            }
          "
        />

        <!-- Date 필터 -->
        <div v-else-if="filter.type === 'date'" class="flex flex-col">
          <label class="text-sm text-gray-600 mb-1">{{ filter.label }}</label>
          <BaseDatePicker
            v-model="localFilters[filter.key]"
            @change="updateFilter(filter.key, localFilters[filter.key])"
          />
        </div>

        <!-- Radio 필터 -->
        <div v-else-if="filter.type === 'radio'" class="flex items-center gap-4">
          <span class="text-sm text-gray-600">{{ filter.label }}</span>
          <div class="flex gap-4">
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
        </div>

        <!-- Checkbox 필터 -->
        <div v-else-if="filter.type === 'checkbox'" class="flex items-center gap-4">
          <span class="text-sm text-gray-600">{{ filter.label }}</span>
          <div class="flex gap-4">
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
      </template>

      <!-- Reset button -->
      <button
        v-if="!isSearchOnly && hasActiveFilters"
        @click="resetFilters"
        class="h-9 px-4 text-sm text-[#698469] hover:bg-[#F5F7F5] rounded-lg border border-[#698469]"
      >
        초기화
      </button>
    </div>
  </div>
</template>
