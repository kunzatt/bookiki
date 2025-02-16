<script setup lang="ts">
import type { TableColumn } from '@/types/common/table';

interface TableProps {
  columns: TableColumn[];
  data: any[];
  hoverable?: boolean;
  loading?: boolean;
  minWidth?: string;
}

const props = withDefaults(defineProps<TableProps>(), {
  hoverable: true,
  loading: false,
  minWidth: '1024px', // 기본 최소 너비 설정
});

const emit = defineEmits<{
  (e: 'row-click', row: any): void;
}>();

const getColumnClass = (column: TableColumn) => {
  return {
    'text-left': column.align === 'left' || !column.align,
    'text-center': column.align === 'center',
    'text-right': column.align === 'right',
    [`w-[${column.width}]`]: column.width,
  };
};
</script>

<template>
  <!-- 테이블 컨테이너에 overflow-x-auto 적용 -->
  <div class="border rounded-lg bg-white">
    <div class="overflow-x-auto">
      <div :style="{ minWidth: minWidth }">
        <table class="w-full">
          <!-- 테이블 헤더 -->
          <thead>
            <tr class="border-b border-gray-200">
              <th
                v-for="column in columns"
                :key="column.key"
                class="py-4 px-6 text-sm font-medium text-gray-600 bg-gray-50 sticky top-0"
                :class="getColumnClass(column)"
                :style="column.width ? { width: column.width } : {}"
              >
                {{ column.label }}
              </th>
            </tr>
          </thead>

          <!-- 테이블 바디 -->
          <tbody>
            <template v-if="loading">
              <tr>
                <td :colspan="columns.length" class="text-center py-8">
                  <div class="flex items-center justify-center">
                    <span class="material-icons animate-spin text-[#698469]">refresh</span>
                  </div>
                </td>
              </tr>
            </template>
            <template v-else-if="data.length === 0">
              <tr>
                <td :colspan="columns.length" class="text-center py-8 text-gray-500">
                  데이터가 없습니다.
                </td>
              </tr>
            </template>
            <template v-else>
              <tr
                v-for="(row, index) in data"
                :key="index"
                class="border-b border-gray-200"
                :class="{ 'hover:bg-gray-50 cursor-pointer': hoverable }"
                @click="emit('row-click', row)"
              >
                <td
                  v-for="column in columns"
                  :key="column.key"
                  class="py-4 px-6 text-sm"
                  :class="getColumnClass(column)"
                >
                  <slot :name="column.key" :row="row">
                    {{ row[column.key] }}
                  </slot>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
