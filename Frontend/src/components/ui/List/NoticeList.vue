<!-- src/components/Notice/NoticeList.vue -->
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { selectAllNotices } from '@/api/notice';
import type { NoticeResponse } from '@/types/api/notice';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import BasicWebTable from '@/components/ui/Table/BasicWebTable.vue';
import BasicFilter from '@/components/ui/Filter/BasicFilter.vue';
import type { FilterConfig } from '@/types/common/filter';
import type { TableColumn } from '@/types/common/table';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// Refs
const notices = ref<NoticeResponse[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);

// Emits definition
const emit = defineEmits<{
  (e: 'delete', id: number): void;
}>();

// Watch notices for debugging
watch(notices, (newValue) => {
  console.log('notices updated:', newValue);
});

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
  { key: 'id', label: '번호', width: '100px', align: 'center' },
  { key: 'title', label: '제목', align: 'left' },
  { key: 'createdAt', label: '등록일', width: '150px', align: 'center' },
  { key: 'viewCount', label: '조회수', width: '100px', align: 'center' },
  { key: 'actions', label: '관리', width: '150px', align: 'center' },
];

// Load notices
const loadNotices = async () => {
  loading.value = true;
  try {
    console.log('Fetching notices with params:', {
      keyword: filterValues.value.keyword,
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    const response = await selectAllNotices({
      keyword: filterValues.value.keyword,
      pageable: {
        page: currentPage.value - 1,
        size: pageSize.value,
        sort: 'createdAt',
        direction: 'desc',
      },
    });

    console.log('API Response:', response);

    if (response && Array.isArray(response)) {
      console.log('Setting array response');
      notices.value = response;
      totalItems.value = response.length;
    } else if (response && response.content) {
      console.log('Setting paginated response');
      notices.value = response.content;
      totalItems.value = response.totalElements;
    } else {
      console.log('No data found, setting empty array');
      notices.value = [];
      totalItems.value = 0;
    }

    console.log('Final notices value:', notices.value);
  } catch (error) {
    console.error('공지사항 목록 조회 실패:', error);
    notices.value = [];
    totalItems.value = 0;
  } finally {
    loading.value = false;
  }
};

// Handle filter apply
const handleFilterApply = () => {
  currentPage.value = 1;
  loadNotices();
};

// Handle page change
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadNotices();
};

// Handle row click
const handleRowClick = (notice: NoticeResponse) => {
  router.push(`/notices/${notice.id}`);
};

onMounted(() => {
  loadNotices();
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
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-lg font-medium">{{ notice.title }}</h3>
          <span class="text-sm text-gray-500">{{ notice.createdAt }}</span>
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
  </div>

  <!-- Desktop Table View -->
  <div class="hidden md:block">
    <BasicFilter :filters="filters" v-model="filterValues" @apply="handleFilterApply" />

    <div class="mt-4">
      <BasicWebTable
        :columns="columns"
        :data="notices || []"
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
  </div>
</template>
