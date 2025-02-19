<template>
  <div class="relative">
    <button
      @click="openModal"
      class="px-4 py-2 text-sm bg-white border rounded-lg hover:bg-gray-50 focus:outline-none datepicker-button"
    >
      {{ selectedDate ? formatDate(selectedDate) : '날짜 선택' }}
    </button>

    <!-- Calendar Modal -->
    <Teleport to="body">
      <div
        v-if="isModalOpen"
        class="fixed inset-0 z-50 overflow-auto bg-gray-500 bg-opacity-50 flex items-center justify-center"
        @click="closeModal"
      >
        <div
          class="bg-white rounded-lg shadow-lg p-4 w-[300px] mx-4"
          @click.stop
        >
          <!-- Calendar Header -->
          <div class="flex justify-between items-center mb-4">
            <button @click="prevMonth" class="p-1 hover:bg-gray-100 rounded">
              <span class="material-icons">chevron_left</span>
            </button>
            <span class="text-sm font-medium">
              {{ currentDate.format('YYYY.MM') }}
            </span>
            <button @click="nextMonth" class="p-1 hover:bg-gray-100 rounded">
              <span class="material-icons">chevron_right</span>
            </button>
          </div>

          <!-- Week Days -->
          <div class="grid grid-cols-7 gap-1 mb-2">
            <div
              v-for="day in weekDays"
              :key="day"
              class="text-center text-xs font-medium text-gray-500"
            >
              {{ day }}
            </div>
          </div>

          <!-- Calendar Days -->
          <div class="grid grid-cols-7 gap-1">
            <button
              v-for="{ date, isCurrentMonth } in daysInMonth"
              :key="date.format('YYYY-MM-DD')"
              @click="selectDate(date)"
              :class="[
                'p-2 text-sm rounded-lg',
                isCurrentMonth ? 'text-gray-900' : 'text-gray-400',
                isSelected(date) ? 'bg-blue-500 text-white hover:bg-blue-600' : 'hover:bg-gray-100',
              ]"
            >
              {{ date.date() }}
            </button>
          </div>

          <!-- Action Buttons -->
          <div class="flex justify-end mt-4 space-x-2">
            <button
              @click="confirmDate"
              class="px-3 py-1 text-sm text-white bg-blue-500 rounded hover:bg-blue-600"
            >
              확인
            </button>
            <button
              @click="closeModal"
              class="px-3 py-1 text-sm text-gray-600 bg-gray-100 rounded hover:bg-gray-200"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import dayjs from 'dayjs';

interface Props {
  modelValue?: Date | null;
  disabled?: boolean;
  format?: string;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  format: 'YYYY.MM.DD',
});

const emit = defineEmits(['update:modelValue', 'change']);

const isModalOpen = ref(false);
const currentDate = ref(dayjs());
const selectedDate = ref<dayjs.Dayjs | null>(
  props.modelValue ? dayjs(props.modelValue) : null
);
const tempSelectedDate = ref<dayjs.Dayjs | null>(null);

const weekDays = ['일', '월', '화', '수', '목', '금', '토'];

// 모달 열기
const openModal = () => {
  if (!props.disabled) {
    isModalOpen.value = true;
    tempSelectedDate.value = selectedDate.value;
  }
};

// 모달 닫기
const closeModal = () => {
  isModalOpen.value = false;
};

// 날짜 계산 로직
const daysInMonth = computed(() => {
  const firstDay = currentDate.value.startOf('month');
  const lastDay = currentDate.value.endOf('month');
  const days = [];

  // 이전 달의 날짜
  const prevMonthDays = firstDay.day();
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    days.push({
      date: firstDay.subtract(i + 1, 'day'),
      isCurrentMonth: false,
    });
  }

  // 현재 달의 날짜
  for (let i = 0; i < lastDay.date(); i++) {
    days.push({
      date: firstDay.add(i, 'day'),
      isCurrentMonth: true,
    });
  }

  // 다음 달의 날짜
  const remainingDays = 42 - days.length; // 6주 x 7일 = 42
  for (let i = 0; i < remainingDays; i++) {
    days.push({
      date: lastDay.add(i + 1, 'day'),
      isCurrentMonth: false,
    });
  }

  return days;
});

const formatDate = (date: dayjs.Dayjs) => {
  return date.format(props.format);
};

const prevMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month');
};

const nextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month');
};

const selectDate = (date: dayjs.Dayjs) => {
  tempSelectedDate.value = date;
};

const confirmDate = () => {
  if (tempSelectedDate.value) {
    selectedDate.value = tempSelectedDate.value;
    emit('update:modelValue', tempSelectedDate.value.toDate());
    emit('change', tempSelectedDate.value.toDate());
  }
  closeModal();
};

const isSelected = (date: dayjs.Dayjs) => {
  return tempSelectedDate.value?.isSame(date, 'day');
};
</script>
