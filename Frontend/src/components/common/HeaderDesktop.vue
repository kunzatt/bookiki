<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { DesktopHeaderProps } from '../../types/common/header';
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
  <header v-if="!isAuthPage" class="sticky top-0 left-0 right-0 bg-white border-b border-gray-200 shadow-sm z-10">
    <div class="ml-64">
      <div class="max-w-[calc(100%-2rem)] mx-auto flex items-center justify-between h-16 px-6">
        <!-- Title -->
        <h1 class="text-xl font-semibold text-gray-800">{{ title }}</h1>

        <!-- Right Icons -->
        <div class="flex items-center space-x-6">
          <!-- Chatbot Button -->
          <button 
            @click="handleChatbot"
            class="flex items-center justify-center w-10 h-10 hover:bg-gray-100 
                   rounded-full transition-colors duration-200"
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
            class="relative flex items-center justify-center w-10 h-10 hover:bg-gray-100 
                   rounded-full transition-colors duration-200"
          >
            <i class="material-icons text-gray-700 !text-[28px]">notifications_none</i>
            <!-- Notification Badge -->
            <span 
              v-if="hasNewNotification"
              class="absolute top-2 right-2 w-2.5 h-2.5 bg-red-500 rounded-full 
                     animate-pulse"
            ></span>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>