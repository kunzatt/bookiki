<script setup lang="ts">
defineOptions({
  name: 'SidebarMenu',
});

import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { MenuItem, User } from '../../types/common/sideBar';
import { menuItems } from '../../types/common/sideBar';
import { useAuthStore } from '@/stores/auth';
import BookikiLogo from '@/assets/BookikiLogo.png';

// Store 사용
const authStore = useAuthStore();

// 각 메뉴의 토글 상태를 관리하는 반응형 객체
const menuStates = ref<{ [key: string]: boolean }>({});

// 사용자 role에 따른 메뉴 필터링
const filteredMenuItems = computed(() => {
  return menuItems.filter(
    (item) =>
      // 권한 없는 메뉴도 포함되도록 수정
      !item.roles.length || (authStore.user && item.roles.includes(authStore.user.role)),
  );
});

// 메뉴 토글 함수
const toggleSubMenu = (menuName: string) => {
  menuStates.value[menuName] = !menuStates.value[menuName];
};

// 현재 메뉴가 열려있는지 확인하는 함수
const isMenuOpen = (menuName: string): boolean => {
  return !!menuStates.value[menuName];
};

// router 인스턴스 생성
const router = useRouter();

// 메뉴 클릭 핸들러 함수 수정
const handleMenuClick = (item: MenuItem) => {
  if (item.name === '홈') {
    if (router.currentRoute.value.path === '/main') {
      window.location.reload();
    } else {
      router.push('/main');
    }
  } else if (item.hasToggle) {
    toggleSubMenu(item.name);
  } else if (item.path) {
    router.push(item.path);
  }
};

const handleSubItemClick = async (subItem: SubMenuItem) => {
  if (subItem.action === 'logout') {
    try {
      await authStore.logout();
      router.push('/login'); // 로그아웃 성공 후 로그인 페이지로 이동
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  } else if (subItem.path) {
    router.push(subItem.path);
  }
};

// 로고 클릭 핸들러 추가
const handleLogoClick = () => {
  if (router.currentRoute.value.path === '/main') {
    window.location.reload();
  } else {
    router.push('/main');
  }
};
</script>

<template>
  <aside class="w-64 h-screen bg-[#F6F6F3] shadow-lg">
    <!-- 로고에 클릭 이벤트 추가 -->
    <div
      class="px-6 py-4 flex flex-col items-center justify-center cursor-pointer"
      @click="handleLogoClick"
    >
      <img :src="BookikiLogo" alt="Bookiki Logo" class="h-32 w-auto" />
    </div>

    <!-- 사용자 환영 메시지 -->
    <div class="px-6 py-3 border-b border-[#F6F6F3]">
      <p class="text-sm text-gray-700">{{ authStore.user?.username || '게스트' }}님 안녕하세요!</p>
    </div>

    <!-- 메뉴 목록 -->
    <nav class="mt-4">
      <ul class="space-y-1">
        <li v-for="item in filteredMenuItems" :key="item.path">
          <!-- 메인 메뉴 아이템 -->
          <div v-if="item.name === '홈'" @click="handleMenuClick(item)" class="block">
            <div
              class="px-6 py-4 flex items-center cursor-pointer hover:bg-[#DAD7CD] transition-colors"
            >
              <span class="material-icons mr-3 text-[#344E41]">{{ item.icon }}</span>
              <span class="text-[#344E41]">{{ item.name }}</span>
            </div>
          </div>

          <div
            v-else
            class="px-6 py-4 flex items-center cursor-pointer hover:bg-[#DAD7CD] transition-colors"
            @click="handleMenuClick(item)"
          >
            <span class="material-icons mr-3 text-[#344E41]">{{ item.icon }}</span>
            <span class="text-[#344E41]">{{ item.name }}</span>
          </div>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<style scoped></style>
