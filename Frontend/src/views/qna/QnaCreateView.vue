<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import BasicMobilePostForm from '@/components/ui/Forms/BasicMobilePostForm.vue';
import BasicWebPostForm from '@/components/ui/Forms/BasicWebPostForm.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import { selectQnaById, createQna, updateQna } from '@/api/qna';
import type { QnaRequest, QnaUpdate } from '@/types/api/qna';
import { QnaType } from '@/types/enums/qnaType';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const isMobile = ref(false);
const isEditMode = ref(false);
const initialFormData = ref({ title: '', content: '' });
const pageTitle = ref('문의사항 작성');

// 컴포넌트 마운트 시 인증 상태 확인 및 초기 데이터 로드
onMounted(async () => {
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
  if (authStore.userRole !== 'ADMIN') {
    console.log('User not authorized, redirecting to home');
    router.push('/');
    return;
  }

  // 수정 모드 체크 및 데이터 로드
  const qnaId = route.params.id;
  if (qnaId) {
    isEditMode.value = true;
    pageTitle.value = '문의사항 수정';
    try {
      const qna = await selectQnaById(Number(qnaId));
      initialFormData.value = {
        title: qna.title,
        content: qna.content,
      };
      console.log('Loaded initial data:', initialFormData.value);
    } catch (error) {
      console.error('Failed to load notice:', error);
      router.push('/notices');
    }
  }
});

const checkDeviceType = () => {
  isMobile.value = window.innerWidth < 768;
};

// 제출 로직
const handleSubmit = async (formData: { title: string; qnaType: QnaType; content: string }) => {
  try {
    // 제출 시점에 다시 한번 인증 상태 확인
    if (!authStore.isAuthenticated) {
      console.log('User not authenticated during submission');
      await router.push('/login');
      return;
    }

    if (isEditMode.value) {
      const updateRequest: QnaUpdate = {
        id: Number(route.params.id),
        title: formData.title,
        qnaType: formData.qnaType,
        content: formData.content,
      };
      await updateQna(updateRequest);
    } else {
      const noticeRequest: QnaRequest = {
        title: formData.title,
        qnaType: formData.qnaType,
        content: formData.content,
      };
      await createQna(noticeRequest);
    }

    await router.push('/qnas');
  } catch (error) {
    console.error(`Failed to ${isEditMode.value ? 'update' : 'create'} qna:`, error);
    if (error.response?.status === 401 || error.response?.status === 302) {
      console.log('Authentication error, redirecting to login');
      await router.push('/login');
    }
  }
};

// 취소 로직
const handleCancel = async () => {
  await router.push('/qnas');
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
        :title="pageTitle"
        type="main"
      />
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" :title="pageTitle" />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <main :class="{ 'mt-4': isMobile }">
            <component
              :is="isMobile ? BasicMobilePostForm : BasicWebPostForm"
              type="qna"
              :initial-data="initialFormData"
              :submit-button-text="isEditMode ? '수정' : '작성'"
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
