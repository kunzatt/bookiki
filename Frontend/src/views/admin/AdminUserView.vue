<!-- AdminUserView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';
import UserList from '@/components/ui/Admin/UserList.vue';

const router = useRouter();
const showDesktopModal = ref(false);

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
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <!-- 메인 컨텐츠 영역 -->
      <div class="max-w-7xl mx-auto">
        <!-- UserList 컴포넌트 -->
        <UserList />
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
