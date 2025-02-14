// QnaList.vue
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { selectQnas } from '@/api/qna';
import type { QnaListResponse } from '@/types/api/qna';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/types/functions/dateFormats';
import { QnaType, QnaTypeDescriptions } from '@/types/enums/qnaType';
import BasicWebPagination from '../Pagination/BasicWebPagination.vue';
import { Pageable, PageResponse } from '@/types/common/pagination';

const router = useRouter();
const authStore = useAuthStore();

const qnas = ref<QnaListResponse[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = ref(10);
const pageInfo = ref<Pageable>({
  pageNumber: 0,
  pageSize: pageSize.value,
  sort: ['createdAt,DESC'],
});
const totalItems = ref(0);

const filters = ref<FilterConfig[]>([
  {
    type: 'search',
    key: 'keyword',
    placeholder: '제목을 입력하세요',
  },
  {
    type: 'select',
    key: 'qnaType',
    options: [
      { value: '', label: '전체' },
      { value: QnaType.NORMAL, label: '일반 문의' },
      { value: QnaType.NEW_BOOK, label: '희망 도서 신청' },
      { value: QnaType.CHANGE_INFO, label: '정보 변경' },
    ],
  },
]);

const filterValues = ref({
  keyword: '',
  qnaType: '',
});

const columns: TableColumn[] = [
  {
    key: 'qnaType',
    label: '유형',
    width: '120px',
    align: 'center',
    render: (row) => {
      const qnaType = row.qnaType as QnaType;
      return {
        component: BasicStatusBadge,
        props: {
          text: QnaTypeDescriptions[qnaType],
          type: getStatusType(qnaType),
          isEnabled: true,
        },
      };
    },
  },
  { key: 'title', label: '제목', align: 'left' },
  { key: 'authorName', label: '작성자', width: '120px', align: 'center' },
  { key: 'createdAt', label: '등록일', width: '120px', align: 'center' },
];

const loadQnas = async () => {
  loading.value = true;
  try {
    const response = await selectQnas({
      keyword: filterValues.value.keyword,
      qnaType: filterValues.value.qnaType,
      pageable: {
        page: currentPage.value - 1,
        size: pageSize.value,
        sort: 'createdAt',
        direction: 'DESC', // 대문자로 변경
      },
    });
    qnas.value = response.content;
    totalItems.value = response.totalElements;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('문의사항 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

const handleFilterApply = () => {
  pageInfo.value = {
    ...pageInfo.value,
    pageNumber: 0,
  };
  currentPage.value = 1;
  loadQnas();
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadQnas(); // 페이지 변경 시 바로 데이터 로드
};

const handleRowClick = (qna: QnaListResponse) => {
  router.push(`/qnas/${qna.id}`);
};

const getStatusType = (qnaType: QnaType): string => {
  switch (qnaType) {
    case QnaType.NORMAL:
      return 'primary';
    case QnaType.NEW_BOOK:
      return 'info';
    case QnaType.CHANGE_INFO:
      return 'warning';
    default:
      return 'gray';
  }
};

watch(currentPage, () => {
  loadQnas();
});

onMounted(() => {
  loadQnas();
});
</script>

<template>
  <div>
    <!-- Mobile List View -->
    <div class="md:hidden">
      <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

      <ul class="space-y-4 mt-4">
        <li
          v-for="qna in qnas"
          :key="qna.id"
          class="bg-white p-4 rounded-lg shadow-sm cursor-pointer"
          @click="handleRowClick(qna)"
        >
          <div class="flex justify-between items-start mb-2">
            <BasicStatusBadge
              :text="QnaTypeDescriptions[qna.qnaType as QnaType] || '기타'"
              :type="getStatusType(qna.qnaType as QnaType)"
              :isEnabled="true"
            />
            <span class="text-sm text-gray-500">
              {{ formatDate(qna.createdAt) }}
            </span>
          </div>
          <div class="mt-2">
            <h3 class="text-base font-medium text-gray-900 mb-1">{{ qna.title }}</h3>
            <p class="text-sm text-gray-500">{{ qna.authorName }}</p>
          </div>
        </li>
      </ul>
    </div>

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
          :total-items="totalItems"
          :total-pages="totalPages"
          :current-page="currentPage"
          :page-size="pageSize"
          @row-click="handleRowClick"
          @page-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>
