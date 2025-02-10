<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { DesktopHeaderProps } from './Header';
import { defineProps, withDefaults } from 'vue';

const props = withDefaults(defineProps<DesktopHeaderProps>(), {
  hasNewNotification: false,
  isAuthPage: false
});

const router = useRouter();

const handleChatbot = () => {
  router.push('/chatbot');
};

const handleNotification = () => {
  router.push('/notifications');
};
</script>

<template>
  <header v-if="!isAuthPage" class="sticky top-0 left-0 right-0 bg-[#F6F6F3] border-b border-gray-100">
    <div class="ml-64"> <!-- 사이드바 공간 -->
      <div class="max-w-[calc(100%-2rem)] mx-auto flex items-center justify-between h-[56px] px-4">
        <!-- Title -->
        <h1 class="text-lg font-medium">{{ title }}</h1>

        <!-- Right Icons -->
        <div class="flex items-center space-x-4">
          <!-- Chatbot Button -->
          <button 
            @click="handleChatbot"
            class="flex items-center justify-center w-10 h-10 hover:bg-gray-100 rounded-full"
          >
            <img 
              src="@/assets/chatbot2.png" 
              alt="chatbot"
              class="w-8 h-8"
            />
          </button>

          <!-- Notification Button -->
          <button 
            @click="handleNotification"
            class="relative flex items-center justify-center w-10 h-10 hover:bg-gray-100 rounded-full"
          >
            <i class="material-icons text-[#344E41] !text-[32px]">notifications_none</i>
            <!-- Notification Badge -->
            <span 
              v-if="hasNewNotification"
              class="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full"
            ></span>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>