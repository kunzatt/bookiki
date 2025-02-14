<script setup lang="ts">
import { useRoute } from 'vue-router';
import QnaDetail from '@/components/ui/Detail/QnaDetail.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import BottomNav from '@/components/common/BottomNav.vue';

const route = useRoute();
const qnaId = Number(route.params.id);
</script>

<template>
  <div class="min-h-screen flex">
    <Sidebar class="hidden md:block fixed h-full" />
    <div class="flex-1 flex flex-col md:ml-64">
      <HeaderMobile
        class="md:hidden fixed top-0 left-0 right-0 z-10"
        title="문의사항"
        type="main"
      />
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="문의사항" />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <QnaDetail :qna-id="qnaId" />
        </div>
      </div>
      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>