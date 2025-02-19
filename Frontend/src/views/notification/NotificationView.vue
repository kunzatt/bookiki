<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ref, onMounted, computed } from 'vue';
import {
  getUserNotifications,
  updateNotificationReadStatus,
  deleteNotification,
} from '@/api/notification';
import { useAuthStore } from '@/stores/auth';
import type { NotificationResponse } from '@/types/api/notification';
import { NotificationStatus } from '@/types/enums/notificationStatus';
import { NotificationType } from '@/types/enums/notificationInformation';

const router = useRouter();
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
      sort: 'createdAt,desc',
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
  // 읽음 처리
  if (notification.notificationStatus === NotificationStatus.UNREAD) {
    try {
      await updateNotificationReadStatus(notification.id);
      notification.notificationStatus = NotificationStatus.READ;
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  }

  // 명시적으로 조건 체크
  if (
    notification.notificationType === 'OVERDUE_NOTICE' ||
    notification.notificationType === NotificationType.OVERDUE_NOTICE
  ) {
    router.push('/mypage/current-borrowed');
    return;
  }

  switch (notification.notificationType) {
    case NotificationType.RETURN_DEADLINE:
      router.push('/mypage/current-borrowed');
      break;

    case NotificationType.QNA_ANSWERED:
    case NotificationType.QNA_CREATED:
      if (notification.notificationId) {
        router.push(`/qnas/${notification.notificationId}`);
      }
      break;

    case NotificationType.FAVORITE_BOOK_AVAILABLE:
      if (notification.notificationId) {
        router.push(`/books/${notification.notificationId}`);
      }
      break;
  }
};

const handleDeleteNotification = async (notificationId: number) => {
  try {
    await deleteNotification(notificationId);
    notifications.value = notifications.value.filter((n) => n.id !== notificationId);
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
  const date = new Date(
    dateArray[0],
    dateArray[1] - 1,
    dateArray[2],
    dateArray[3] || 0,
    dateArray[4] || 0,
    dateArray[5] || 0,
  );

  if (isNaN(date.getTime())) {
    console.error('Invalid date array:', dateArray);
    return '날짜 없음';
  }

  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minute = 60 * 1000;
  const hour = minute * 60;
  const day = hour * 24;

  if (diff < hour) {
    const minutes = Math.floor(diff / minute);
    return `${minutes}분 전`;
  } else if (diff < day) {
    const hours = Math.floor(diff / hour);
    return `${hours}시간 전`;
  } else if (diff < day * 7) {
    const days = Math.floor(diff / day);
    return `${days}일 전`;
  }
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
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <main class="flex-1 px-5 lg:px-8 pb-16 lg:pb-8 overflow-y-auto">
        <div class="max-w-3xl mx-auto">
          <!-- 데스크톱 타이틀 -->
          <div class="hidden lg:flex justify-between items-center p-4 mb-4">
            <div class="flex items-center space-x-4"></div>
            <span class="text-sm text-[#698469]">전체 {{ totalElements }}개</span>
          </div>

          <!-- 알림 리스트 -->
          <div class="bg-white rounded-lg shadow-sm divide-y divide-[#E8EDE8]">
            <TransitionGroup name="list" tag="div">
              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="hover:bg-[#F5F7F5] transition-colors"
                :class="{
                  'bg-[#E8EDE8]': notification.notificationStatus === 'UNREAD',
                }"
              >
                <div class="p-4">
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center space-x-2">
                      <span
                        class="w-2 h-2 rounded-full"
                        :class="
                          notification.notificationStatus === 'UNREAD'
                            ? 'bg-[#698469]'
                            : 'bg-gray-300'
                        "
                      ></span>
                    </div>
                    <button
                      @click="handleDeleteNotification(notification.id)"
                      class="text-gray-400 hover:text-[#698469] transition-colors"
                    >
                      <span class="sr-only">삭제</span>
                      <i class="material-icons !text-[20px]">close</i>
                    </button>
                  </div>

                  <div
                    @click="handleNotificationClick(notification)"
                    class="cursor-pointer hover:text-[#698469] transition-colors"
                  >
                    <p class="text-gray-900 mb-1">{{ notification.content }}</p>
                    <p class="text-sm text-[#698469]">
                      {{ formatDate(notification.createdAt) }}
                    </p>
                  </div>
                </div>
              </div>
            </TransitionGroup>

            <!-- 로딩 상태 -->
            <div v-if="loading" class="p-8 flex justify-center">
              <div
                class="w-6 h-6 border-2 border-[#698469] rounded-full animate-spin border-t-transparent"
              ></div>
            </div>

            <!-- 빈 상태 -->
            <div
              v-if="!loading && notifications.length === 0"
              class="p-8 text-center text-[#698469]"
            >
              알림이 없습니다.
            </div>
          </div>

          <!-- 페이지네이션 -->
          <div v-if="totalPages > 1" class="flex justify-center mt-4">
            <div class="flex items-center justify-center gap-1">
              <!-- 이전 페이지 버튼 -->
              <button
                class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-100"
                :class="{ 'text-gray-300': !canGoPrevious }"
                :disabled="!canGoPrevious"
                @click="handlePageChange(currentPage - 1)"
                aria-label="Previous page"
              >
                <span class="material-icons text-sm">chevron_left</span>
              </button>

              <!-- 페이지 번호 -->
              <button
                v-for="page in pageNumbers"
                :key="page"
                class="w-8 h-8 flex items-center justify-center rounded text-sm"
                :class="{
                  'bg-[#698469] text-white': page === currentPage,
                  'hover:bg-gray-100': page !== currentPage,
                }"
                @click="handlePageChange(page)"
                :aria-label="`Go to page ${page + 1}`"
                :aria-current="page === currentPage ? 'page' : undefined"
              >
                {{ page + 1 }}
              </button>

              <!-- 다음 페이지 버튼 -->
              <button
                class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-100"
                :class="{ 'text-gray-300': !canGoNext }"
                :disabled="!canGoNext"
                @click="handlePageChange(currentPage + 1)"
                aria-label="Next page"
              >
                <span class="material-icons text-sm">chevron_right</span>
              </button>
            </div>
          </div>
        </div>
      </main>
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
