[App.vue]
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import Footer from './components/common/Footer.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 인증 페이지 목록
const authPages = ['/login', '/signup', '/forgot-password'];

// 현재 페이지가 인증 페이지인지 확인
const isAuthPage = computed(() => {
  return authPages.includes(route.path);
});

// 페이지 제목 관리
const pageTitles: { [key: string]: string } = {
  '/': '홈',
  '/main': '홈',
  '/library': '가상도서관',
  '/notices': '공지사항',
  '/notifications': '알림',
  '/qnas': '문의사항',
  '/mypage': '마이페이지',
  '/admin': '관리자',
  '/admin/user': '사용자 관리',
  '/admin/books': '도서 관리',
  '/admin/library': '도서관 관리',
  '/password/change': '비밀번호 변경',
  '/feedbacks': '피드백 관리',
  '/search': '도서 검색',
};

// 현재 페이지 제목
const pageTitle = computed(() => {
  return pageTitles[route.path] || '북키키';
});

onMounted(() => {
  // sessionStorage에서 유저 정보 복구
  authStore.initializeFromStorage();

  // 현재 경로가 인증이 필요한 페이지인데 인증되지 않은 경우 로그인 페이지로 리다이렉트
  const currentRoute = router.currentRoute.value;
  if (currentRoute.meta.requiresAuth && !authStore.isAuthenticated) {
    router.push('/login');
  }
});
</script>

<template>
  <div id="app" class="min-h-screen">
    <!-- 인증 페이지일 경우 레이아웃 없이 라우터 뷰만 표시 -->
    <router-view v-if="isAuthPage" />

    <!-- 일반 페이지 레이아웃 -->
    <div v-else class="min-h-screen flex">
      <!-- 데스크톱 사이드바 -->
      <Sidebar class="hidden md:block fixed h-full" />

      <div class="flex-1 flex flex-col md:ml-64">
        <!-- 모바일/데스크톱 헤더 -->
        <HeaderMobile
          class="md:hidden fixed top-0 left-0 right-0 z-10"
          :title="pageTitle"
          type="main"
        />
        <HeaderDesktop
          class="hidden md:block fixed top-0 right-0 left-64 z-10"
          :title="pageTitle"
        />

        <!-- 메인 콘텐츠 영역 -->
        <main class="flex-1 mt-16 md:mt-20 px-4 md:px-20 pb-20 md:pb-20">
          <div class="mx-auto w-full min-w-[320px] md:min-w-[768px] max-w-[1280px]">
            <router-view />
          </div>
        </main>

        <!-- 모바일 하단 네비게이션 -->
        <div class="md:hidden fixed bottom-0 left-0 right-0">
          <BottomNav />
        </div>
      </div>
    </div>
    <Footer class="hidden md:block" />
  </div>
</template>

<style>
html,
body {
  height: 100%;
  margin: 0;
}

#app {
  height: 100%;
}
</style>
