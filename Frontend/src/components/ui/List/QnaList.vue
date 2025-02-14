<!-- src/components/Notice/NoticeList.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { selectQnas } from '@/api/qna';
import { QnaListResponse } from '@/types/api/qna';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/types/functions/dateFormats';
import { QnaType } from '@/types/enums/qnaType';
import { QnaTypeDescriptions } from '@/types/enums/qnaType';

const router = useRouter();
const authStore = useAuthStore();

const qnas = ref<QnaListResponse[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// Filter configuration
const filters = ref<FilterConfig[]>([
  {
    type: 'search',
    key: 'keyword',
    label: '검색',
    placeholder: '제목 또는 내용을 입력하세요',
  },
]);

const filterValues = ref({
  keyword: '',
});

// Table columns configuration
const columns: TableColumn[] = [
  { key: 'title', label: '제목', align: 'left' },
  { key: 'createdAt', label: '등록일', width: '150px', align: 'center' },
  { key: 'actions', label: '관리', width: '150px', align: 'center' },
];

// Load notices
const loadQnas = async () => {
  loading.value = true;
  try {
    const response = await selectQnas({
      keyword: filterValues.value.keyword,
      pageable: {
        page: currentPage.value - 1,
        size: pageSize.value,
        sort: 'createdAt',
        direction: 'desc',
      },
    });
    qnas.value = response.content;
    totalItems.value = response.totalElements;
  } catch (error) {
    console.error('공지사항 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

// Handle filter apply
const handleFilterApply = () => {
  currentPage.value = 1;
  loadQnas();
};

// Handle page change
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadQnas();
};

// Handle row click
const handleRowClick = (qna: QnaListResponse) => {
  router.push(`/qnas/${qna.id}`);
};

const getStatusType = (qnaType: QnaType): BadgeStatus => {
  switch (qnaType) {
    case QnaType.NORMAL:
      return 'primary'; // 일반 문의 → 기본 색상
    case QnaType.NEW_BOOK:
      return 'info'; // 희망 도서 신청 → 정보성
    case QnaType.CHANGE_INFO:
      return 'warning'; // 이름/사번 변경 → 주의 필요
    default:
      return 'gray'; // 기타 → 회색
  }
};

onMounted(() => {
  loadQnas();
});
</script>

<template>
  <!-- Mobile List View -->
  <div class="md:hidden">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <ul class="space-y-4">
      <li v-for="(item, index) in items" :key="index" class="bg-white p-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-start mb-2">
          <!-- 상태 배지 -->
          <BasicStatusBadge
            :text="item.qnaType"
            :type="getStatusType(item.qnaType)"
            :isEnabled="true"
          />
          <!-- 날짜 -->
          <span class="text-sm text-gray-500">
            {{ formatDate(item.createdAt) }}
          </span>
        </div>

        <!-- 제목과 작성자 정보 -->
        <div class="mt-2">
          <h3 class="text-base font-medium text-gray-900 mb-1">
            {{ item.title }}
          </h3>
          <p class="text-sm text-gray-500">
            {{ item.quthorName }}
          </p>
        </div>
      </li>
    </ul>

    <!-- Desktop Table View -->
    <div class="hidden md:block">
      <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

      <div class="mt-4">
        <BasicWebTable
          :columns="columns"
          :data="
            qnas.map((qna) => ({
              ...qna,
              createdAt: formatDate(qna.createdAt),
            }))
          "
          :loading="loading"
          @row-click="handleRowClick"
        >
          <template #actions="{ row }">
            <div v-if="authStore.userRole == 'ADMIN'" class="flex justify-center gap-2">
              <button class="text-sm text-red-500" @click.stop="$emit('delete', row.id)">
                삭제
              </button>
            </div>
          </template>
        </BasicWebTable>
      </div>
    </div>
  </div>
</template>
