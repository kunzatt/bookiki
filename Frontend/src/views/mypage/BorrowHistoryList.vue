<template>
  <div class="flex h-screen overflow-hidden bg-gray-50">
    <!-- 데스크톱 사이드바 - lg 이상에서만 표시 -->
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 반응형 헤더 -->
      <HeaderMobile class="lg:hidden" title="나의 대출 이력" />
      <HeaderDesktop class="hidden lg:block" title="나의 대출 이력" />

      <main class="flex-1 px-4 lg:px-8 pb-20 lg:pb-8 overflow-y-auto">
        <div class="w-full max-w-4xl mx-auto">
          <div class="flex justify-between items-center my-6">
            <h1 class="text-xl lg:text-2xl font-medium">나의 대출 이력</h1>
            <span class="text-gray-600">총 {{ totalElements }}권</span>
          </div>

          <!-- 필터 섹션 -->
          <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
              <!-- 기간 선택 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">기간</label>
                <select
                  v-model="selectedPeriod"
                  class="w-full border border-gray-300 rounded-md px-3 py-2"
                  @change="handlePeriodChange"
                >
                  <option v-for="(desc, type) in PeriodTypeDescriptions" :key="type" :value="type">
                    {{ desc }}
                  </option>
                </select>
              </div>

              <!-- 커스텀 기간 선택 -->
              <div v-if="selectedPeriod === PeriodType.CUSTOM" class="lg:col-span-2 grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">시작일</label>
                  <input
                    type="date"
                    v-model="startDate"
                    class="w-full border border-gray-300 rounded-md px-3 py-2"
                    @change="handleDateChange"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">종료일</label>
                  <input
                    type="date"
                    v-model="endDate"
                    class="w-full border border-gray-300 rounded-md px-3 py-2"
                    @change="handleDateChange"
                  />
                </div>
              </div>

              <!-- 연체 여부 필터 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">연체 여부</label>
                <select
                  v-model="overdueFilter"
                  class="w-full border border-gray-300 rounded-md px-3 py-2"
                  @change="fetchHistories"
                >
                  <option value="">전체</option>
                  <option :value="true">연체</option>
                  <option :value="false">정상 반납</option>
                </select>
              </div>
            </div>
          </div>

          <!-- 로딩 상태 -->
          <div v-if="isLoading" class="flex justify-center items-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
          </div>

          <!-- 에러 메시지 -->
          <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {{ error }}
          </div>

          <!-- 대출 이력 목록 -->
          <div v-else>
            <div v-if="histories.length === 0" class="text-center py-8 text-gray-500">
              대출 이력이 없습니다.
            </div>
            <BookHistoryList
              v-else
              :items="histories"
            />

            <!-- 페이지네이션 -->
            <div v-if="histories.length > 0" class="mt-6 flex justify-center space-x-2">
              <button
                class="px-4 py-2 border rounded-md disabled:opacity-50"
                :disabled="currentPage === 0"
                @click="handlePageChange(currentPage - 1)"
              >
                이전
              </button>
              <span class="px-4 py-2">
                {{ currentPage + 1 }} / {{ totalPages }}
              </span>
              <button
                class="px-4 py-2 border rounded-md disabled:opacity-50"
                :disabled="currentPage >= totalPages - 1"
                @click="handlePageChange(currentPage + 1)"
              >
                다음
              </button>
            </div>
          </div>
        </div>
      </main>

      <!-- 모바일 하단 네비게이션 - lg 미만에서만 표시 -->
      <div class="block lg:hidden">
        <BottomNav />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BookHistoryList from '@/components/ui/List/BookHistoryList.vue';
import { getUserBookHistories } from '@/api/bookHistory';
import { PeriodType, PeriodTypeDescriptions } from '@/types/enums/periodType';
import type { BookHistoryResponse } from '@/types/api/bookHistory';

// 상태 관리
const isLoading = ref(true);
const error = ref<string | null>(null);
const histories = ref<BookHistoryResponse[]>([]);
const selectedPeriod = ref<PeriodType>(PeriodType.LAST_MONTH);
const startDate = ref('');
const endDate = ref('');
const overdueFilter = ref<boolean | ''>('');
const currentPage = ref(0);
const totalPages = ref(1);
const totalElements = ref(0);
const pageSize = 10;

// 대출 이력 조회
const fetchHistories = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    const params = {
      periodType: selectedPeriod.value,
      startDate: selectedPeriod.value === PeriodType.CUSTOM ? startDate.value : undefined,
      endDate: selectedPeriod.value === PeriodType.CUSTOM ? endDate.value : undefined,
      overdue: overdueFilter.value === '' ? undefined : overdueFilter.value,
      page: currentPage.value,
      size: pageSize,
      sort: 'borrowedAt,desc'
    };

    console.log('대출 이력 조회 요청 파라미터:', params);
    const response = await getUserBookHistories(params);
    console.log('대출 이력 조회 응답:', response);
    
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

// 이벤트 핸들러
const handlePeriodChange = () => {
  if (selectedPeriod.value !== PeriodType.CUSTOM) {
    startDate.value = '';
    endDate.value = '';
    currentPage.value = 0;
    fetchHistories();
  }
};

const handleDateChange = () => {
  if (selectedPeriod.value === PeriodType.CUSTOM) {
    if (startDate.value && endDate.value) {
      currentPage.value = 0;
      fetchHistories();
    }
  }
};

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage;
  fetchHistories();
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchHistories();
});
</script>
