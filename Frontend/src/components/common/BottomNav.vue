<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 py-2">
    <div class="max-w-screen-xl mx-auto px-4">
      <div class="flex justify-around items-center">
        <router-link 
          to="/" 
          class="flex flex-col items-center text-sm"
          :class="$route.path === '/' ? 'text-blue-600' : 'text-gray-500'"
        >
          <i class="fas fa-home text-xl mb-1"></i>
          <span>홈</span>
        </router-link>
        
        <router-link 
          to="/search" 
          class="flex flex-col items-center text-sm"
          :class="$route.path === '/search' ? 'text-blue-600' : 'text-gray-500'"
        >
          <i class="fas fa-search text-xl mb-1"></i>
          <span>검색</span>
        </router-link>
        
        <router-link 
          to="/mypage" 
          class="flex flex-col items-center text-sm"
          :class="$route.path.startsWith('/mypage') ? 'text-blue-600' : 'text-gray-500'"
        >
          <i class="fas fa-user text-xl mb-1"></i>
          <span>내 정보</span>
        </router-link>
        
        <router-link 
          to="/settings" 
          class="flex flex-col items-center text-sm"
          :class="$route.path === '/settings' ? 'text-blue-600' : 'text-gray-500'"
        >
          <i class="fas fa-cog text-xl mb-1"></i>
          <span>설정</span>
        </router-link>
      </div>
    </div>
  </nav>
  
  <!-- 하단 여백 확보를 위한 스페이서 -->
  <div class="h-16"></div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { navItems } from '@/types/common/bottomNav';
import { useAuthStore } from '@/stores/auth';
import { useRoute } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();
const route = useRoute();

// 현재 활성화된 메뉴를 추적
const activeTab = ref(router.currentRoute.value.path);
</script>

<style scoped>
.material-icons {
  font-size: 24px;
}
</style>