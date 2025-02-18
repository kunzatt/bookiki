[components/common/HeaderDesktop.vue]
<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { DesktopHeaderProps } from '../../types/common/header';
import { defineProps, withDefaults } from 'vue';
import { ref, onMounted, onUnmounted } from 'vue';
import { hasUnreadNotifications } from '@/api/notification';
import ChatBot from '../ui/ChatBot/ChatBot.vue';

const props = withDefaults(defineProps<DesktopHeaderProps>(), {
  hasNewNotification: false,
  isAuthPage: false,
});

const router = useRouter();
const hasUnreadNotification = ref(false);
const isChatbotOpen = ref(false);

const handleChatbot = () => {
  isChatbotOpen.value = !isChatbotOpen.value;
};

const checkUnreadNotifications = async () => {
  try {
    hasUnreadNotification.value = await hasUnreadNotifications();
  } catch (error) {
    console.error('알림 상태 확인 실패:', error);
  }
};

const handleChatbotClose = () => {
  isChatbotOpen.value = false;
};

const handleNotification = () => {
  router.push('/notifications');
};

const handleNotice = () => {
  router.push('/notices');
};

onMounted(() => {
  checkUnreadNotifications();
  const interval = setInterval(checkUnreadNotifications, 60000);
  onUnmounted(() => {
    clearInterval(interval);
  });
});
</script>

<template>
  <header
    v-if="!isAuthPage"
    class="sticky top-0 left-0 right-0 bg-white border-b border-gray-200 shadow-sm z-10"
  >
    <div class="ml-10">
      <!-- h-16을 h-20으로 변경 -->
      <div class="relative flex items-center h-20 px-6">
        <div class="flex-1 min-w-0 max-w-[calc(100%-160px)]">
          <!-- text-xl을 text-2xl로 변경 -->
          <h1 class="text-2xl font-semibold text-gray-800 truncate">{{ title }}</h1>
        </div>
        <!-- 아이콘 컨테이너의 높이도 맞춰서 변경 -->
        <div
          class="fixed top-0 transition-all duration-200 flex items-center h-20 space-x-6"
          :style="{ right: '24px' }"
        >
          <!-- Chatbot Button -->
          <button
            @click="handleChatbot"
            class="flex items-center justify-center w-10 h-10 hover:bg-gray-100 rounded-full transition-colors duration-200"
          >
            <img src="@/assets/chatbot2.png" alt="chatbot" class="w-8 h-8" />
          </button>

          <!-- Notice Button -->
          <button
            @click="handleNotice"
            class="flex items-center justify-center w-10 h-10 hover:bg-gray-100 rounded-full transition-colors duration-200"
          >
            <i class="material-icons text-gray-700 !text-[28px]">announcement</i>
          </button>

          <!-- Notification Button -->
          <button
            @click="handleNotification"
            class="relative flex items-center justify-center w-10 h-10 hover:bg-gray-100 rounded-full transition-colors duration-200"
          >
            <i class="material-icons text-gray-700 !text-[28px]">notifications_none</i>
            <span
              v-if="hasUnreadNotification"
              class="absolute top-2 right-2 w-2.5 h-2.5 bg-red-500 rounded-full animate-pulse"
            ></span>
          </button>
        </div>
      </div>
    </div>

    <!-- Chatbot Component -->
    <ChatBot
      :modelValue="isChatbotOpen"
      @update:modelValue="isChatbotOpen = $event"
      @close="handleChatbotClose"
    />
  </header>
</template>
