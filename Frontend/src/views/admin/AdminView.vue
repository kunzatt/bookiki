<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Button from '@/components/ui/Button/BasicButton.vue';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';

const router = useRouter();
const showDesktopModal = ref(false);

const menuItems = [
  { id: 1, title: '도서 관리', path: '/admin/books' },
  { id: 2, title: '도서관 관리', path: '/admin/library' },
  { id: 3, title: '회원 관리', path: '/admin/user' },
  { id: 4, title: '문의사항', path: '/qnas' },
  { id: 5, title: '알림', path: '/notifications' },
];

// 메뉴 아이템 클릭 핸들러
const handleMenuClick = async (path: string) => {
  router.push(path);
};

// 모바일 체크 함수
const isMobileDevice = () => {
  return window.innerWidth < 1024;
};

// 컴포넌트 마운트 시 모바일 체크
onMounted(() => {
  if (isMobileDevice()) {
    showDesktopModal.value = true;
  }

  // 리사이즈 이벤트 리스너
  window.addEventListener('resize', () => {
    if (isMobileDevice()) {
      showDesktopModal.value = true;
    }
  });
});

// 모달 닫기 핸들러
const handleModalClose = () => {
  router.push('/'); // 메인 페이지로 리다이렉트
};
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <HeaderMobile class="lg:hidden" />
      <HeaderDesktop class="hidden lg:block" />

      <main class="flex-1 px-5 lg:px-8 py-8 lg:pb-8 overflow-y-auto">
        <div class="max-w-[1440px] mx-auto">
          <div class="grid grid-cols-2 gap-4">
            <Button
              v-for="item in menuItems"
              :key="item.id"
              size="L"
              :isEnabled="false"
              :text="item.title"
              class="flex items-center justify-between"
              @click="router.push(item.path)"
            >
              <template #default>
                <span>{{ item.title }}</span>
                <span class="text-gray-400">›</span>
              </template>
            </Button>
          </div>
        </div>
      </main>

      <div class="lg:hidden">
        <BottomNav />
      </div>
    </div>

    <!-- 데스크탑 전용 알림 모달 -->
    <BaseModal
      v-model="showDesktopModal"
      title="데스크탑 환경에서 확인해주세요"
      content="이 페이지는 데스크탑 환경에서만 이용 가능합니다. PC에서 다시 접속해 주시기 바랍니다."
      icon="desktop_windows"
      confirm-text="메인으로 이동"
      @update:model-value="handleModalClose"
    />
  </div>
</template>
