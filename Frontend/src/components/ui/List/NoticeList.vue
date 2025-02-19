<!-- src/components/Notice/NoticeList.vue -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { selectAllNotices } from '@/api/notice';
import type { NoticeResponse } from '@/types/api/notice';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/types/functions/dateFormats';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import type { Pageable } from '@/types/common/pagination';

const router = useRouter();
const authStore = useAuthStore();

const notices = ref<NoticeResponse[]>([]);
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
    placeholder: '검색어를 입력하세요',
  },
]);

const filterValues = ref({
  keyword: '',
});

// Table columns configuration
const columns: TableColumn[] = [
  { key: 'title', label: '제목', align: 'left' },
  { key: 'createdAt', label: '등록일', width: '150px', align: 'center' },
  { key: 'viewCount', label: '조회수', width: '100px', align: 'center' },
  { key: 'actions', label: '관리', width: '150px', align: 'center' },
];

// Load notices
const loadNotices = async () => {
  loading.value = true;
  try {
    const response = await selectAllNotices({
      keyword: filterValues.value.keyword,
      pageable: {
        page: isMobile.value ? Math.floor(notices.value.length / pageSize.value) : 0,
        size: pageSize.value,
        sort: [''],
      },
    });

    if (isMobile.value) {
      notices.value =
        notices.value.length === 0 ? response.content : [...notices.value, ...response.content];
    } else {
      notices.value = response.content;
    }

    totalItems.value = response.totalElements;
    hasMore.value = notices.value.length < response.totalElements;
  } catch (error) {
    console.error('공지사항 목록 조회 실패:', error);
  } finally {
    loading.value = false;
    isLoadingMore.value = false;
  }
};

// Handle filter apply
const handleFilterApply = () => {
  notices.value = [];
  hasMore.value = true;
  currentPage.value = 1;
  loadNotices();
};

// Handle row click
const handleRowClick = (notice: NoticeResponse) => {
  console.log('Clicked notice:', notice);
  router.push(`/notices/${notice.id}`);
};

const handlePaginationChange = async (pageInfo: Pageable) => {
  if (!isMobile.value) {
    // 데스크톱에서만 처리
    currentPage.value = pageInfo.pageNumber + 1;
    loading.value = true;
    try {
      const response = await selectAllNotices({
        keyword: filterValues.value.keyword,
        pageable: {
          page: pageInfo.pageNumber,
          size: pageSize.value,
          sort: [''],
        },
      });
      notices.value = response.content;
      totalItems.value = response.totalElements;
    } catch (error) {
      console.error('공지사항 목록 조회 실패:', error);
    } finally {
      loading.value = false;
    }
  }
};

// 모바일 스크롤 용
const isMobile = ref(window.innerWidth < 768);
const observerTarget = ref<HTMLElement | null>(null);
const hasMore = ref(true); // 더 불러올 데이터가 있는지 확인
const isLoadingMore = ref(false); // 추가 데이터 로딩 중인지 확인
const allNotices = ref<NoticeResponse[]>([]); // 모바일용 전체 데이터 저장
let observer: IntersectionObserver | null = null;

// 디바이스 타입 변경 감지 및 처리
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
      notices.value = [];
      loadNotices();
    }
    // 데스크톱에서 모바일로 전환 시
    else if (!wasMobile && newIsMobile) {
      observer = setupIntersectionObserver();
      loadNotices();
    }
  }
};

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
      if (
        entry.isIntersecting &&
        !isLoadingMore.value &&
        !loading.value &&
        hasMore.value &&
        isMobile.value
      ) {
        isLoadingMore.value = true;
        loadNotices();
      }
    });
  }, options);

  nextTick(() => {
    if (observerTarget.value) {
      observer.observe(observerTarget.value);
    }
  });

  return observer;
};

defineEmits(['delete']);

onMounted(() => {
  checkDeviceType();
  window.addEventListener('resize', checkDeviceType);
  loadNotices();

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
</script>

<template>
  <!-- Mobile List View -->
  <div class="md:hidden">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <div class="mt-4 space-y-4">
      <div
        v-for="notice in notices"
        :key="notice.id"
        class="bg-white p-4 rounded-lg shadow-sm cursor-pointer"
        @click="handleRowClick(notice)"
      >
        <div class="flex justify-between items-start mb-2 gap-4">
          <h3 class="text-lg font-medium">{{ notice.title }}</h3>
          <span class="text-sm text-gray-500">{{ formatDate(notice.createdAt) }}</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">조회수: {{ notice.viewCount }}</span>
          <div v-if="authStore.userRole == 'ADMIN'" class="flex gap-2">
            <button class="text-sm text-red-500" @click.stop="$emit('delete', notice.id)">
              삭제
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- 무한 스크롤 감지 영역 -->
    <div ref="observerTarget" class="h-20 flex justify-center items-center">
      <div v-if="isLoadingMore" class="text-gray-500">로딩 중...</div>
      <div v-else-if="!hasMore" class="text-gray-500">모든 공지사항을 불러왔습니다</div>
    </div>
  </div>

  <!-- Desktop Table View -->
  <div class="hidden md:block">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <div class="mt-4">
      <BasicWebTable
        :columns="columns"
        :data="
          notices.map((notice) => ({
            ...notice,
            createdAt: formatDate(notice.createdAt),
          }))
        "
        :loading="loading"
        @row-click="handleRowClick"
      >
        <template #actions="{ row }">
          <div v-if="authStore.userRole == 'ADMIN'" class="flex justify-center gap-2">
            <button class="text-sm text-red-500" @click.stop="$emit('delete', row.id)">삭제</button>
          </div>
        </template>
      </BasicWebTable>
    </div>
    <!-- 데스크톱 페이지네이션 추가 -->
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
