<script setup lang="ts">
import { defineProps, withDefaults, ref } from 'vue';
import type { MobileHeaderProps } from '../../types/common/header';
import { useRouter } from 'vue-router';
import ChatBot from '../ui/ChatBot/ChatBot.vue';

const router = useRouter();
const isChatbotOpen = ref(false);

const props = withDefaults(defineProps<MobileHeaderProps>(), {
  type: 'default',
  hasNewNotification: false,
});

const handleBack = () => {
  router.back();
};

const handleNotification = () => {
  router.push('/notifications');
};

const handleNotice = () => {
  router.push('/notices');
};

const handleChatbot = () => {
  isChatbotOpen.value = !isChatbotOpen.value;
};

const handleChatbotClose = () => {
  isChatbotOpen.value = false;
};
</script>

<template>
  <header class="sticky top-0 left-0 right-0 bg-white border-b border-gray-100">
    <div class="flex items-center justify-between h-14 px-4">
      <!-- Back Button (Only for default type) -->
      <button
        v-if="type === 'default'"
        @click="handleBack"
        class="flex items-center justify-center w-8 h-8"
      >
        <i class="material-icons">arrow_back_ios</i>
      </button>
      <div v-else-if="type === 'main'" class="w-8"></div>
      <div v-else class="w-8"></div>

      <!-- Title -->
      <h1 class="flex-1 text-center text-lg font-medium">{{ title }}</h1>

      <!-- Notification Bell (For default and main types) -->
      <div v-if="type === 'default' || type === 'main'" class="flex items-center w-auto">
        <!-- Chatbot Button -->
        <button @click="handleChatbot" class="flex items-center justify-center w-8 h-8">
          <img src="@/assets/chatbot2.png" alt="chatbot" class="w-6 h-6" />
        </button>

        <!-- Notice Button -->
        <button @click="handleNotice" class="flex items-center justify-center w-8 h-8">
          <i class="material-icons">announcement</i>
        </button>

        <!-- Notification Button -->
        <button
          @click="handleNotification"
          class="relative flex items-center justify-center w-8 h-8"
        >
          <i class="material-icons">notifications_none</i>
          <!-- Notification Badge -->
          <span
            v-if="hasNewNotification"
            class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"
          ></span>
        </button>
      </div>
      <div v-else class="w-8"></div>
    </div>

    <!-- Chatbot Component -->
    <ChatBot
      :modelValue="isChatbotOpen"
      @update:modelValue="isChatbotOpen = $event"
      @close="handleChatbotClose"
    />
  </header>
</template>
