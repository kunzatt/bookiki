<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { selectFeedbacks } from '@/api/chatBot';
import type { ChatbotFeedbackResponse } from '@/types/api/chatbot';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';
import { formatDate } from '@/types/functions/dateFormats';
import BasicWebPagination from '@/components/ui/Pagination/BasicWebPagination.vue';
import type { Pageable } from '@/types/common/pagination';
import { FeedbackCategory, FeedbackCategoryDescriptions } from '@/types/enums/feedbackCategory';
import { FeedbackStatus, FeedbackStatusDescriptions } from '@/types/enums/feedbackStatus';

const router = useRouter();

const feedbacks = ref<ChatbotFeedbackResponse[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const hasMore = ref(true);

const filters = ref<FilterConfig[]>([
  {
    type: 'select',
    key: 'category',
    label: '피드백 유형',
    options: [
      { value: '', label: '전체' },
      { value: FeedbackCategory.QR_ERROR, label: 'QR 코드 오류' },
      { value: FeedbackCategory.LED_ERROR, label: 'LED 위치 표시 오류' },
      { value: FeedbackCategory.CAMERA_ERROR, label: '카메라 인식 오류' },
      { value: FeedbackCategory.BOOK_LOCATION, label: '도서 위치 오류' },
      { value: FeedbackCategory.SYSTEM_ERROR, label: '시스템 오류' },
      { value: FeedbackCategory.CHATBOT_ERROR, label: '챗봇 응답 오류' },
      { value: FeedbackCategory.OTHER, label: '기타' },
    ],
  },
  {
    type: 'select',
    key: 'status',
    label: '처리 상태',
    options: [
      { value: '', label: '전체' },
      { value: FeedbackStatus.PENDING, label: '처리 대기' },
      { value: FeedbackStatus.IN_PROGRESS, label: '처리 중' },
      { value: FeedbackStatus.COMPLETED, label: '처리 완료' },
    ],
  },
]);

const filterValues = ref({
  keyword: '',
  category: '',
  status: '',
});

const getFeedbackCategory = (category: string) => {
  const categoryMap = {
    SYSTEM_ERROR: '시스템 오류',
    LED_ERROR: 'LED 위치 표시 오류',
    CAMERA_ERROR: '카메라 인식 오류',
    BOOK_LOCATION: '도서 위치 오류',
    CHATBOT_ERROR: '챗봇 응답 오류',
    OTHER: '기타',
  };
  return categoryMap[category] || category;
};

const getFeedbackStatus = (status: string) => {
  const statusMap = {
    PENDING: '처리 대기',
    IN_PROGRESS: '처리 중',
    COMPLETED: '처리 완료',
  };
  return statusMap[status] || status;
};

const formatTableDate = (dateArray: number[]) => {
  if (!Array.isArray(dateArray)) return '';
  const [year, month, day] = dateArray;
  return `${year}.${String(month).padStart(2, '0')}.${String(day).padStart(2, '0')}`;
};

// 데이터 변환
const processedFeedbacks = computed(() => {
  return feedbacks.value.map((feedback) => ({
    ...feedback,
    category: getFeedbackCategory(feedback.category),
    status: getFeedbackStatus(feedback.status),
    createdAt: formatTableDate(feedback.createdAt),
  }));
});

const columns: TableColumn[] = [
  {
    key: 'category',
    label: '유형',
    width: '240px',
    align: 'center',
  },
  {
    key: 'feedbackMessage',
    label: '피드백',
    align: 'left',
  },
  {
    key: 'userId',
    label: '작성자 ID',
    width: '160px',
    align: 'center',
  },
  {
    key: 'createdAt',
    label: '등록일',
    width: '120px',
    align: 'center',
  },
  {
    key: 'status',
    label: '처리',
    width: '120px',
    align: 'center',
  },
];

const loadFeedbacks = async () => {
  loading.value = true;
  try {
    const response = await selectFeedbacks({
      keyword: filterValues.value.keyword || undefined,
      category: filterValues.value.category || undefined,
      status: filterValues.value.status || undefined,
      pageable: {
        page: isMobile.value ? 0 : currentPage.value - 1,
        size: pageSize.value,
        sort: ['createdAt,DESC'],
      },
    });

    feedbacks.value = response.content;
    // 서버에서 받은 전체 아이템 수를 사용
    totalItems.value = response.totalElements;
    // hasMore 값도 서버 응답을 기반으로 설정
    hasMore.value = !response.last;
  } catch (error) {
    console.error('피드백 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

const handleFilterApply = () => {
  currentPage.value = 1;
  loadFeedbacks();
};

const handleRowClick = (feedback: ChatbotFeedbackResponse) => {
  // 라우터 이동 전에 이벤트가 완전히 처리되도록 보장
  nextTick(() => {
    router.push({
      name: 'FeedbackDetail',
      params: { id: feedback.id },
    });
  });
};

const handlePaginationChange = async (pageInfo: Pageable) => {
  if (!isMobile.value) {
    currentPage.value = pageInfo.pageNumber + 1;
    await loadFeedbacks();
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
      feedbacks.value = [];
      loadFeedbacks();
    }
    // 데스크톱에서 모바일로 전환 시
    else if (!wasMobile && newIsMobile) {
      observer = setupIntersectionObserver();
      loadFeedbacks();
    }
  }
};

onMounted(() => {
  checkDeviceType();
  window.addEventListener('resize', checkDeviceType);
  loadFeedbacks();

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
        loadMoreFeedbacks();
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
const loadMoreFeedbacks = async () => {
  if (isLoadingMore.value || !hasMore.value) return;

  isLoadingMore.value = true;
  try {
    const nextPage = Math.floor(feedbacks.value.length / pageSize.value);
    const response = await selectFeedbacks({
      keyword: filterValues.value.keyword,
      category: filterValues.value.category,
      status: filterValues.value.status,
      pageable: {
        page: nextPage,
        size: pageSize.value,
        sort: ['createdAt,DESC'],
      },
    });

    if (response.content.length > 0) {
      feedbacks.value = [...feedbacks.value, ...response.content];
      hasMore.value = feedbacks.value.length < response.totalElements;
    } else {
      hasMore.value = false;
    }
  } catch (error) {
    console.error('추가 피드백 로드 실패:', error);
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
        v-for="feedback in feedbacks"
        :key="feedback.id"
        class="bg-white p-3 rounded-lg shadow-sm cursor-pointer"
        @click="handleRowClick(feedback)"
      >
        <div class="flex justify-between items-center">
          <BasicStatusBadge
            :text="FeedbackCategoryDescriptions[feedback.category as FeedbackCategory]"
            type="category"
            :isEnabled="true"
            class="min-w-[8rem]"
          />
          <div class="flex flex-col items-end">
            <span class="text-sm text-gray-500 mb-1">{{ formatDate(feedback.createdAt) }}</span>
            <BasicStatusBadge
              :text="FeedbackStatusDescriptions[feedback.status as FeedbackStatus]"
              type="status"
              :isEnabled="true"
            />
          </div>
        </div>
        <div class="mt-2">
          <p class="text-sm">{{ feedback.feedbackMessage }}</p>
        </div>
      </div>
    </div>
    <!-- 무한 스크롤 감지 영역 -->
    <div ref="observerTarget" class="h-20 flex justify-center items-center">
      <div v-if="isLoadingMore" class="text-gray-500">로딩 중...</div>
      <div v-else-if="!hasMore" class="text-gray-500">모든 피드백을 불러왔습니다</div>
    </div>
  </div>

  <!-- Desktop Table View -->
  <div class="hidden md:block">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <div class="mt-4">
      <BasicWebTable
        :columns="columns"
        :data="processedFeedbacks"
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
