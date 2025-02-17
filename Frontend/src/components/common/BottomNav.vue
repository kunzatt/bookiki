<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { navItems } from '@/types/common/bottomNav';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// 현재 활성화된 메뉴를 추적
const activeTab = ref(router.currentRoute.value.path);
</script>

<template>
    <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200">
      <div class="flex justify-around items-center h-16">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex flex-col items-center w-full py-1"
          :class="{ 'text-[#344E41]': activeTab === item.path, 'text-[#A3B18A]': activeTab !== item.path }"
          @click="activeTab = item.path"
        >
          <span class="material-icons text-2xl">{{ item.icon }}</span>
          <span class="text-xs mt-1">{{ item.name }}</span>
        </router-link>
      </div>
    </nav>
  
    <!-- 하단 여백 확보를 위한 스페이서 -->
    <div class="h-16"></div>
  </template>

<style scoped>
.material-icons {
  font-size: 24px;
}
</style>
