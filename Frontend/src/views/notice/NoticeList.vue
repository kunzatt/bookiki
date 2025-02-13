<script setup lang="ts">
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';



</script>

<template>
  <div class="min-h-screen flex">
    <!-- 사이드바 (데스크톱) -->
    <Sidebar class="hidden md:block fixed h-full" />

    <!-- 메인 컨텐츠 영역 -->
    <div class="flex-1 flex flex-col md:ml-64">
      <!-- 모바일 헤더 -->
      <HeaderMobile class="md:hidden fixed top-0 left-0 right-0 z-10" title="공지사항 작성" type="main" />
      
      <!-- 데스크톱 헤더 -->
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="공지사항 작성" />

      <!-- 스크롤 가능한 컨텐츠 영역 -->
      <div class="flex-1 overflow-y-auto pt-16"> <!-- 헤더 높이만큼 패딩 추가 -->
        <div class="h-full p-4 md:p-8">
            <!-- Main Content -->

        </div>
      </div>

      <!-- 모바일 하단 네비게이션 -->
      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>