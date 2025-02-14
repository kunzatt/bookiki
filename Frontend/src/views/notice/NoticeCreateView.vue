<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import BasicMobilePostForm from '@/components/ui/Forms/BasicMobilePostForm.vue';
import BasicWebPostForm from '@/components/ui/Forms/BasicWebPostForm.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import { createNotice } from '@/api/notice';
import type { NoticeRequest } from '@/types/api/notice';

const router = useRouter();
const authStore = useAuthStore();
const isMobile = ref(false);

// 컴포넌트 마운트 시 인증 상태 확인
onMounted(() => {
  checkDeviceType();
  window.addEventListener('resize', checkDeviceType);

  // 스토어에서 인증 상태 초기화
  authStore.initializeFromStorage();

  // 인증되지 않은 경우 로그인 페이지로 리다이렉트
  if (!authStore.isAuthenticated) {
    console.log('User not authenticated, redirecting to login');
    router.push('/login');
    return;
  }

  // 관리자 권한 체크
  if (authStore.userRole !== 'ROLE_ADMIN') {
    console.log('User not authorized, redirecting to home');
    router.push('/');
    return;
  }
});

const checkDeviceType = () => {
  isMobile.value = window.innerWidth < 768;
};

// 제출 로직
const handleSubmit = async (formData: { title: string; content: string }) => {
  try {
    // 제출 시점에 다시 한번 인증 상태 확인
    if (!authStore.isAuthenticated) {
      console.log('User not authenticated during submission');
      await router.push('/login');
      return;
    }

    console.log('handleSubmit called with:', formData);

    const noticeRequest: NoticeRequest = {
      title: formData.title,
      content: formData.content,
    };

    console.log('Calling createNotice with:', noticeRequest);
    const result = await createNotice(noticeRequest);
    console.log('Notice created with ID:', result);

    await router.push('/notices');
  } catch (error) {
    console.error('Failed to create notice:', error);
    if (error.response?.status === 401 || error.response?.status === 302) {
      console.log('Authentication error, redirecting to login');
      await router.push('/login');
    }
  }
};

// 취소 로직
const handleCancel = async () => {
  await router.push('/notices');
};

onUnmounted(() => {
  window.removeEventListener('resize', checkDeviceType);
});
</script>

<template>
  <div class="min-h-screen flex">
    <Sidebar class="hidden md:block fixed h-full" />
    <div class="flex-1 flex flex-col md:ml-64">
      <HeaderMobile
        class="md:hidden fixed top-0 left-0 right-0 z-10"
        title="공지사항 작성"
        type="main"
      />
      <HeaderDesktop
        class="hidden md:block fixed top-0 right-0 left-64 z-10"
        title="공지사항 작성"
      />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <main :class="{ 'mt-4': isMobile }">
            <component
              :is="isMobile ? BasicMobilePostForm : BasicWebPostForm"
              type="notice"
              @submit="handleSubmit"
              @cancel="handleCancel"
            />
          </main>
        </div>
      </div>
      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>
