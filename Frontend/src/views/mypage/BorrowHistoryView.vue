<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import dayjs from 'dayjs';
import BookHistoryList from '@/components/ui/List/BookHistoryList.vue';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import BasicSelect from '@/components/ui/Select/BasicSelect.vue';
import { getUserBookHistories } from '@/api/bookHistory';
import { PeriodType, PeriodTypeDescriptions } from '@/types/enums/periodType';
import type { BookHistoryResponse } from '@/types/api/bookHistory';
import type { SelectOption } from '@/types/common/select';

// 상태 관리
const isLoading = ref(true);
const error = ref<string | null>(null);
const histories = ref<BookHistoryResponse[]>([]);
const selectedPeriod = ref<PeriodType>(PeriodType.LAST_MONTH);
const startDate = ref('');
const endDate = ref('');
const overdueFilter = ref<boolean | ''>('');
const currentPage = ref(1);
const totalPages = ref(1);
const totalElements = ref(0);
const pageSize = 8;

// 캘린더 모달 관련 상태
const isModalOpen = ref(false);
const currentDate = ref(dayjs());
const selectedType = ref<'start' | 'end'>('start');
const tempStartDate = ref('');
const tempEndDate = ref('');
const activeTab = ref('custom');

// Select 옵션 생성
const periodOptions = Object.entries(PeriodTypeDescriptions).map(([value, label]) => ({
  value,
  label,
}));

const overdueOptions: SelectOption[] = [
  { value: '', label: '전체' },
  { value: 'true', label: '연체' },
  { value: 'false', label: '정상 반납' },
];

// 달력 관련 설정
const weekDays = ['일', '월', '화', '수', '목', '금', '토'];

const daysInMonth = computed(() => {
  const firstDay = currentDate.value.startOf('month');
  const daysInMonth = currentDate.value.daysInMonth();
  const startOfWeek = firstDay.day();

  const days = [];
  // 이전 달의 마지막 날짜들
  for (let i = 0; i < startOfWeek; i++) {
    days.push({
      date: firstDay.subtract(startOfWeek - i, 'day'),
      isCurrentMonth: false,
    });
  }

  // 현재 달의 날짜들
  for (let i = 1; i <= daysInMonth; i++) {
    days.push({
      date: firstDay.add(i - 1, 'day'),
      isCurrentMonth: true,
    });
  }

  // 다음 달의 시작 날짜들
  const remainingDays = 42 - days.length;
  for (let i = 1; i <= remainingDays; i++) {
    days.push({
      date: firstDay.add(daysInMonth - 1 + i, 'day'),
      isCurrentMonth: false,
    });
  }

  return days;
});

// 기간 선택 탭 옵션
const tabOptions = [
  {
    key: 'month1',
    label: '최근 1개월',
    value: () => {
      const end = dayjs();
      const start = end.subtract(1, 'month');
      return [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')];
    },
  },
  {
    key: 'month3',
    label: '최근 3개월',
    value: () => {
      const end = dayjs();
      const start = end.subtract(3, 'month');
      return [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')];
    },
  },
  {
    key: 'custom',
    label: '사용자 지정',
    value: () => [tempStartDate.value, tempEndDate.value],
  },
];

// 대출 이력 조회
const fetchHistories = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    const params = {
      periodType: selectedPeriod.value,
      startDate: selectedPeriod.value === PeriodType.CUSTOM ? startDate.value : undefined,
      endDate: selectedPeriod.value === PeriodType.CUSTOM ? endDate.value : undefined,
      overdue: overdueFilter.value === '' ? undefined : overdueFilter.value === 'true',
      page: currentPage.value - 1,
      size: pageSize,
      sort: 'borrowedAt,desc',
    };

    const response = await getUserBookHistories(params);
    histories.value = response.content;
    totalPages.value = Math.ceil(response.totalElements / pageSize);
    totalElements.value = response.totalElements;
  } catch (err: any) {
    console.error('대출 이력 조회 실패:', err);
    error.value = '대출 이력을 불러오는데 실패했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const handlePeriodChange = (value: string) => {
  selectedPeriod.value = value as PeriodType;
  if (selectedPeriod.value !== PeriodType.CUSTOM) {
    startDate.value = '';
    endDate.value = '';
    currentPage.value = 1;
    fetchHistories();
  }
};

const handleTabChange = (tab: string) => {
  activeTab.value = tab;
  const selectedOption = tabOptions.find((option) => option.key === tab);
  if (selectedOption && tab !== 'custom') {
    const [start, end] = selectedOption.value();
    tempStartDate.value = start;
    tempEndDate.value = end;
  }
};

const openModal = (type: 'start' | 'end') => {
  selectedType.value = type;
  isModalOpen.value = true;
};

const prevMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month');
};

const nextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month');
};

const selectDate = (date: dayjs.Dayjs) => {
  if (selectedType.value === 'start') {
    tempStartDate.value = date.format('YYYY-MM-DD');
    selectedType.value = 'end';
  } else {
    if (date.isBefore(tempStartDate.value)) {
      tempEndDate.value = tempStartDate.value;
      tempStartDate.value = date.format('YYYY-MM-DD');
    } else {
      tempEndDate.value = date.format('YYYY-MM-DD');
    }
  }
};

const confirmDateSelection = () => {
  startDate.value = tempStartDate.value;
  endDate.value = tempEndDate.value;
  isModalOpen.value = false;
  if (startDate.value && endDate.value) {
    currentPage.value = 1;
    fetchHistories();
  }
};

const isSelected = (date: dayjs.Dayjs) => {
  const formattedDate = date.format('YYYY-MM-DD');
  return formattedDate === tempStartDate.value || formattedDate === tempEndDate.value;
};

const isInRange = (date: dayjs.Dayjs) => {
  if (!tempStartDate.value || !tempEndDate.value) return false;
  const formattedDate = date.format('YYYY-MM-DD');
  return formattedDate > tempStartDate.value && formattedDate < tempEndDate.value;
};

const handlePageInfoUpdate = (pageInfo: any) => {
  currentPage.value = pageInfo.pageNumber + 1;
  fetchHistories();
};

onMounted(() => {
  fetchHistories();
});
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="flex justify-end my-6">
        <span class="text-gray-600">총 {{ totalElements }}권</span>
      </div>

      <!-- 필터 섹션 -->
      <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <!-- 기간 선택 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">기간</label>
            <BasicSelect
              v-model="selectedPeriod"
              :options="periodOptions"
              size="L"
              @update:modelValue="handlePeriodChange"
            />
          </div>

          <!-- 커스텀 기간 선택 -->
          <div
            v-if="selectedPeriod === PeriodType.CUSTOM"
            class="lg:col-span-2 grid grid-cols-2 gap-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">시작일</label>
              <div class="relative">
                <div
                  class="w-full px-4 py-2 bg-[#F5F7F5] rounded text-sm text-[#698469] cursor-pointer"
                  @click="openModal('start')"
                >
                  {{ startDate || '시작일' }}
                </div>
                <span
                  class="absolute right-3 top-1/2 transform -translate-y-1/2 material-icons text-[#698469]"
                >
                  calendar_today
                </span>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">종료일</label>
              <div class="relative">
                <div
                  class="w-full px-4 py-2 bg-[#F5F7F5] rounded text-sm text-[#698469] cursor-pointer"
                  @click="openModal('end')"
                >
                  {{ endDate || '종료일' }}
                </div>
                <span
                  class="absolute right-3 top-1/2 transform -translate-y-1/2 material-icons text-[#698469]"
                >
                  calendar_today
                </span>
              </div>
            </div>
          </div>

          <!-- 연체 여부 필터 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">연체 여부</label>
            <BasicSelect
              v-model="overdueFilter"
              :options="overdueOptions"
              size="L"
              @update:modelValue="fetchHistories"
            />
          </div>
        </div>
      </div>

      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="flex justify-center items-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-[#698469]"></div>
      </div>

      <!-- 에러 메시지 -->
      <div
        v-else-if="error"
        class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4"
      >
        {{ error }}
      </div>

      <!-- 대출 이력 목록 -->
      <div v-else>
        <div v-if="histories.length === 0" class="text-center py-8 text-gray-500">
          대출 이력이 없습니다.
        </div>
        <BookHistoryList v-else :items="histories" />

        <!-- 페이지네이션 -->
        <div v-if="histories.length > 0" class="mt-6 flex justify-center">
          <BasicWebPagination
            :current-page="currentPage"
            :total-pages="totalPages"
            :page-size="pageSize"
            :sort="['borrowedAt,DESC']"
            @update:pageInfo="handlePageInfoUpdate"
          />
        </div>
      </div>
    </div>

    <!-- 캘린더 모달 -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="isModalOpen" class="fixed inset-0 flex items-center justify-center z-50">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black bg-opacity-40" @click="isModalOpen = false" />

        <!-- Modal Content -->
        <div class="relative bg-white rounded-2xl p-6 max-w-md w-full mx-4 z-10">
          <!-- 기간 선택 탭 -->
          <div class="mb-6">
            <div class="flex gap-2">
              <button
                v-for="tab in tabOptions"
                :key="tab.key"
                @click="handleTabChange(tab.key)"
                class="flex-1 py-2 text-xs font-medium rounded-full"
                :class="[
                  activeTab === tab.key ? 'bg-[#698469] text-white' : 'bg-[#F5F7F5] text-[#698469]',
                ]"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <!-- 달력 -->
          <div v-if="activeTab === 'custom'">
            <!-- 달력 헤더 -->
            <div class="flex items-center justify-between mb-4">
              <button @click="prevMonth" class="p-1">
                <span class="material-icons text-[#698469]">chevron_left</span>
              </button>
              <span class="text-sm font-medium text-[#698469]">
                {{ currentDate.format('YYYY년 MM월') }}
              </span>
              <button @click="nextMonth" class="p-1">
                <span class="material-icons text-[#698469]">chevron_right</span>
              </button>
            </div>

            <!-- 요일 헤더 -->
            <div class="grid grid-cols-7 mb-2">
              <div
                v-for="day in weekDays"
                :key="day"
                class="text-center text-xs text-[#A3B8A3] py-2"
              >
                {{ day }}
              </div>
            </div>

            <!-- 날짜 그리드 -->
            <div class="grid grid-cols-7 gap-1">
              <button
                v-for="{ date, isCurrentMonth } in daysInMonth"
                :key="date.format('YYYY-MM-DD')"
                @click="selectDate(date)"
                class="aspect-square flex items-center justify-center text-sm rounded-full relative"
                :class="[
                  isCurrentMonth ? 'text-[#2C3E2E]' : 'text-[#CCD5CC]',
                  isSelected(date) ? 'bg-[#698469] text-white' : '',
                  isInRange(date) ? 'bg-[#E8EDE8]' : '',
                ]"
              >
                {{ date.format('D') }}
                <div
                  v-if="isSelected(date)"
                  class="absolute -bottom-1 w-1 h-1 bg-[#698469] rounded-full"
                />
              </button>
            </div>

            <!-- 선택 상태 표시 -->
            <div class="mt-4 text-xs text-[#698469] text-center">
              {{ selectedType === 'start' ? '시작일' : '종료일' }} 선택
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="flex justify-center gap-3 mt-6">
            <button
              class="px-4 py-2 bg-[#698469] text-white rounded-lg text-sm"
              @click="confirmDateSelection"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.material-icons {
  font-size: 20px;
}

button:focus {
  outline: none;
}

/* 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
