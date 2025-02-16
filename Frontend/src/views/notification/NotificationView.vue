<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import {
  getUserNotifications,
  updateNotificationReadStatus,
  deleteNotification,
} from '@/api/notification';
import { useAuthStore } from '@/stores/auth';
import type { NotificationResponse } from '@/types/api/notification';
import { NotificationStatus } from '@/types/enums/notificationStatus';

const authStore = useAuthStore();
const notifications = ref<NotificationResponse[]>([]);
const loading = ref(true);
const currentPage = ref(0);
const pageSize = 10;
const totalPages = ref(0);
const totalElements = ref(0);

// 페이지네이션을 위한 computed 속성들
const canGoPrevious = computed(() => currentPage.value > 0);
const canGoNext = computed(() => currentPage.value < totalPages.value - 1);
const pageNumbers = computed(() => {
  const pages = [];
  const maxVisiblePages = 5;
  let startPage = Math.max(0, currentPage.value - Math.floor(maxVisiblePages / 2));
  const endPage = Math.min(totalPages.value - 1, startPage + maxVisiblePages - 1);

  if (endPage - startPage + 1 < maxVisiblePages) {
    startPage = Math.max(0, endPage - maxVisiblePages + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
});

const loadNotifications = async (page: number = currentPage.value) => {
  if (!authStore.isAuthenticated) {
    console.log('인증 안됨:', authStore.user);
    return;
  }

  loading.value = true;
  try {
    const requestConfig = {
      page,
      size: pageSize,
      sort: 'createdAt,desc'
    };

    const response = await getUserNotifications(requestConfig);

    if (response) {
      notifications.value = response.content;
      totalPages.value = response.totalPages;
      totalElements.value = response.totalElements;
      currentPage.value = page;
    }
  } catch (error) {
    console.error('알림 로드 실패:', error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    loadNotifications(page);
  }
};

const handleNotificationClick = async (notification: NotificationResponse) => {
  if (notification.notificationStatus === NotificationStatus.UNREAD) {
    try {
      await updateNotificationReadStatus(notification.id);
      notification.notificationStatus = NotificationStatus.READ;
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  }
};

const handleDeleteNotification = async (notificationId: number) => {
  try {
    await deleteNotification(notificationId);
    notifications.value = notifications.value.filter((n) => n.id !== notificationId);
    // 마지막 항목을 삭제했고 현재 페이지가 마지막이 아닌 경우 새로고침
    if (notifications.value.length === 0 && currentPage.value > 0) {
      handlePageChange(currentPage.value - 1);
    } else {
      loadNotifications(currentPage.value);
    }
  } catch (error) {
    console.error('알림 삭제 실패:', error);
  }
};

const formatDate = (dateArray: number[]): string => {
  // 배열을 Date 객체로 변환 (주의: 월은 0-based 이므로 1을 빼줌)
  const date = new Date(
    dateArray[0], // 년
    dateArray[1] - 1, // 월 (0-based)
    dateArray[2], // 일
    dateArray[3] || 0, // 시
    dateArray[4] || 0, // 분
    dateArray[5] || 0, // 초
  );

  // 날짜가 유효한지 확인
  if (isNaN(date.getTime())) {
    console.error('Invalid date array:', dateArray);
    return '날짜 없음';
  }

  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minute = 60 * 1000;
  const hour = minute * 60;
  const day = hour * 24;

  // 1시간 이내
  if (diff < hour) {
    const minutes = Math.floor(diff / minute);
    return `${minutes}분 전`;
  }
  // 24시간 이내
  else if (diff < day) {
    const hours = Math.floor(diff / hour);
    return `${hours}시간 전`;
  }
  // 7일 이내
  else if (diff < day * 7) {
    const days = Math.floor(diff / day);
    return `${days}일 전`;
  }
  // 그 외
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
};

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadNotifications();
  }
});
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 기존 헤더 부분 유지 -->
    <div class="lg:hidden sticky top-0 bg-white border-b z-10">
      <div class="px-4 py-3 flex items-center justify-between">
        <h1 class="text-lg font-medium">알림</h1>
      </div>
    </div>

    <div class="max-w-3xl mx-auto">
      <div class="hidden lg:flex justify-between items-center p-4 mb-4">
        <h1 class="text-xl font-semibold">알림</h1>
        <span class="text-sm text-gray-500">전체 {{ totalElements }}개</span>
      </div>

      <!-- 알림 리스트 -->
      <div class="divide-y divide-gray-200">
        <TransitionGroup name="list" tag="div">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="bg-white hover:bg-gray-50 transition-colors"
            :class="{
              'bg-blue-50 hover:bg-blue-100': notification.notificationStatus === 'UNREAD',
            }"
          >
            <div class="p-4">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center space-x-2">
                  <span
                    class="w-2 h-2 rounded-full"
                    :class="
                      notification.notificationStatus === 'UNREAD' ? 'bg-blue-500' : 'bg-gray-300'
                    "
                  ></span>
                </div>
                <button
                  @click="handleDeleteNotification(notification.id)"
                  class="text-gray-400 hover:text-gray-600"
                >
                  <span class="sr-only">삭제</span>
                  <i class="material-icons !text-[20px]">close</i>
                </button>
              </div>

              <div @click="handleNotificationClick(notification)" class="cursor-pointer">
                <p class="text-gray-900 mb-1">{{ notification.content }}</p>
                <p class="text-sm text-gray-500">
                  {{ formatDate(notification.createdAt) }}
                </p>
              </div>
            </div>
          </div>
        </TransitionGroup>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="p-8 flex justify-center">
          <div
            class="w-6 h-6 border-2 border-blue-500 rounded-full animate-spin border-t-transparent"
          ></div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="!loading && notifications.length === 0" class="p-8 text-center text-gray-500">
          알림이 없습니다.
        </div>

        <!-- 페이지네이션 -->
        <div v-if="totalPages > 1" class="flex justify-center items-center space-x-2 p-4">
          <button
            @click="handlePageChange(currentPage - 1)"
            :disabled="!canGoPrevious"
            class="px-3 py-1 rounded border"
            :class="canGoPrevious ? 'hover:bg-gray-100' : 'opacity-50 cursor-not-allowed'"
          >
            이전
          </button>

          <button
            v-for="page in pageNumbers"
            :key="page"
            @click="handlePageChange(page)"
            class="px-3 py-1 rounded border"
            :class="currentPage === page ? 'bg-blue-500 text-white' : 'hover:bg-gray-100'"
          >
            {{ page + 1 }}
          </button>

          <button
            @click="handlePageChange(currentPage + 1)"
            :disabled="!canGoNext"
            class="px-3 py-1 rounded border"
            :class="canGoNext ? 'hover:bg-gray-100' : 'opacity-50 cursor-not-allowed'"
          >
            다음
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.list-move {
  transition: transform 0.3s ease;
}
</style>
