<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { selectQnas } from '@/api/qna';
import type { QnaListResponse } from '@/types/api/qna';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/types/functions/dateFormats';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import type { Pageable } from '@/types/common/pagination';
import { QnaType, QnaTypeDescriptions } from '@/types/enums/qnaType';

const router = useRouter();

const qnas = ref<QnaListResponse[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const hasMore = ref(true);

const filters = ref<FilterConfig[]>([
  {
    type: 'search',
    key: 'keyword',
    label: '검색',
    placeholder: '제목을 입력하세요',
  },
  {
    type: 'select',
    key: 'qnaType',
    label: '문의 유형',
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
    render: (row) => ({
      component: BasicStatusBadge,
      props: {
        text: QnaTypeDescriptions[row.qnaType as QnaType],
        type: getStatusType(row.qnaType as QnaType),
        isEnabled: true,
      },
    }),
  },
  { key: 'title', label: '제목', align: 'left' },
  { key: 'authorName', label: '작성자', width: '120px', align: 'center' },
  { key: 'createdAt', label: '등록일', width: '120px', align: 'center' },
];

const loadQnas = async () => {
  loading.value = true;
  try {
    // 모바일과 데스크톱의 페이지 처리 구분
    const pageNumber = isMobile.value ? 0 : currentPage.value - 1;

    const response = await selectQnas({
      keyword: filterValues.value.keyword,
      qnaType: filterValues.value.qnaType,
      pageable: {
        page: pageNumber,
        size: pageSize.value,
        sort: ['createdAt,DESC'],
      },
    });

    // 모바일일 경우 누적, 데스크톱일 경우 교체
    if (isMobile.value && currentPage.value > 1) {
      qnas.value = [...qnas.value, ...response.content];
    } else {
      qnas.value = response.content;
    }

    totalItems.value = response.totalElements;
    hasMore.value = qnas.value.length < response.totalElements;
  } catch (error) {
    console.error('문의사항 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

const handleFilterApply = () => {
  currentPage.value = 1;
  loadQnas();
};

const handleRowClick = (qna: QnaListResponse) => {
  router.push(`/qnas/${qna.id}`);
};

const handlePaginationChange = async (pageInfo: Pageable) => {
  if (!isMobile.value) {
    currentPage.value = pageInfo.pageNumber + 1;
    await loadQnas();
  }
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

// 모바일 관련 설정
const isMobile = ref(window.innerWidth < 768);
const observerTarget = ref<HTMLElement | null>(null);
const isLoadingMore = ref(false);
let observer: IntersectionObserver | null = null;

const checkDeviceType = () => {
  const wasMobile = isMobile.value;
  const newIsMobile = window.innerWidth < 768;

  // 모바일/데스크톱 전환이 있을 때만 처리
  if (wasMobile !== newIsMobile) {
    isMobile.value = newIsMobile;

    // 모바일에서 데스크톱으로 전환 시
    if (wasMobile && !newIsMobile) {
      if (observer) {
        observer.disconnect();
        observer = null;
      }
      currentPage.value = 1;
      qnas.value = [];
      loadQnas();
    }
    // 데스크톱에서 모바일로 전환 시
    else if (!wasMobile && newIsMobile) {
      observer = setupIntersectionObserver();
      loadQnas();
    }
  }
};

onMounted(() => {
  checkDeviceType();
  window.addEventListener('resize', checkDeviceType);
  loadQnas();

  if (isMobile.value) {
    observer = setupIntersectionObserver();
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', checkDeviceType);
  if (observer) {
    observer.disconnect();
  }
});

// Intersection Observer 설정
const setupIntersectionObserver = () => {
  if (observer) {
    observer.disconnect();
  }

  const options = {
    root: null,
    rootMargin: '20px',
    threshold: 0.1,
  };

  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting && !isLoadingMore.value && hasMore.value) {
        loadMoreQnas();
      }
    });
  }, options);

  // nextTick을 사용하여 DOM 업데이트 후 observe
  nextTick(() => {
    if (observerTarget.value) {
      observer.observe(observerTarget.value);
    }
  });

  return observer;
};

// 모바일 무한 스크롤
const loadMoreQnas = async () => {
  if (isLoadingMore.value || !hasMore.value) return;

  isLoadingMore.value = true;
  try {
    const nextPage = Math.floor(qnas.value.length / pageSize.value);
    const response = await selectQnas({
      keyword: filterValues.value.keyword,
      qnaType: filterValues.value.qnaType,
      pageable: {
        page: nextPage,
        size: pageSize.value,
        sort: ['createdAt,DESC'],
      },
    });

    if (response.content.length > 0) {
      qnas.value = [...qnas.value, ...response.content];
      hasMore.value = qnas.value.length < response.totalElements;
    } else {
      hasMore.value = false;
    }
  } catch (error) {
    console.error('추가 문의사항 로드 실패:', error);
  } finally {
    isLoadingMore.value = false;
  }
};

defineEmits(['delete']);
</script>

<template>
  <!-- Mobile List View -->
  <div class="md:hidden">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <div class="mt-4 space-y-4">
      <div
        v-for="qna in qnas"
        :key="qna.id"
        class="bg-white p-4 rounded-lg shadow-sm cursor-pointer"
        @click="handleRowClick(qna)"
      >
        <div class="flex justify-between items-start mb-2">
          <BasicStatusBadge
            :text="QnaTypeDescriptions[qna.qnaType as QnaType]"
            :type="getStatusType(qna.qnaType as QnaType)"
            :isEnabled="true"
          />
          <span class="text-sm text-gray-500">{{ formatDate(qna.createdAt) }}</span>
        </div>
        <div class="mt-2">
          <h3 class="text-base font-medium text-gray-900 mb-1">{{ qna.title }}</h3>
          <p class="text-sm text-gray-500">{{ qna.authorName }}</p>
        </div>
      </div>
    </div>
    <!-- 무한 스크롤 감지 영역 -->
    <div ref="observerTarget" class="h-20 flex justify-center items-center">
      <div v-if="isLoadingMore" class="text-gray-500">로딩 중...</div>
      <div v-else-if="!hasMore" class="text-gray-500">모든 문의사항을 불러왔습니다</div>
    </div>
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
        @row-click="handleRowClick"
      />
    </div>
    <!-- 데스크톱 페이지네이션 -->
    <div class="mt-4 flex justify-center">
      <BasicWebPagination
        :current-page="currentPage"
        :total-pages="Math.ceil(totalItems / pageSize)"
        :page-size="pageSize"
        @update:pageInfo="handlePaginationChange"
      />
    </div>
  </div>
</template>

<style scoped>
.md:hidden {
  padding-bottom: env(safe-area-inset-bottom, 16px);
}
</style>
