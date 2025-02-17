<!-- AdminBookView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';
import BookList from '@/components/ui/Admin/BookList.vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';

const router = useRouter();
const showDesktopModal = ref(false);
const searchKeyword = ref('');

// 메뉴 아이템 클릭 핸들러
const handleMenuClick = async (path: string) => {
  router.push(path);
};

// 검색 핸들러
const handleSearch = () => {
  // 검색 로직 실행
  console.log('Search keyword:', searchKeyword.value);
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
  router.push('/');
};
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <HeaderMobile title="도서 관리" class="lg:hidden" />
      <HeaderDesktop title="도서 관리" class="hidden lg:block" />

      <!-- 메인 컨텐츠 영역 -->
      <main class="flex-1 overflow-auto p-6">
        <div class="max-w-7xl mx-auto">
          <div class="flex justify-between items-center mb-6">
            <!-- 검색바 -->
            <div class="w-96">
              <BasicInput
                type="withButton"
                v-model="searchKeyword"
                placeholder="도서명, ISBN, 출판사, 저자로 검색"
                buttonText="검색"
                @button-click="handleSearch"
              />
            </div>
          </div>

          <!-- BookList 컴포넌트 -->
          <BookList :keyword="searchKeyword" />
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
