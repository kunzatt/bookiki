<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import BasicMobilePostForm from '@/components/ui/Forms/BasicMobilePostForm.vue';
import BasicWebPostForm from '@/components/ui/Forms/BasicWebPostForm.vue';
import { createNotice, updateNotice, selectNoticeById } from '@/api/notice';
import type { NoticeRequest, NoticeUpdate } from '@/types/api/notice';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const isMobile = ref(false);
const isEditMode = ref(false);
const initialFormData = ref({ title: '', content: '' });
const pageTitle = ref('공지사항 작성');

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
  const noticeId = route.params.id;
  if (noticeId) {
    isEditMode.value = true;
    pageTitle.value = '공지사항 수정';
    try {
      const notice = await selectNoticeById(Number(noticeId));
      initialFormData.value = {
        title: notice.title,
        content: notice.content,
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

const handleSubmit = async (formData: { title: string; content: string }) => {
  try {
    if (!authStore.isAuthenticated) {
      console.log('User not authenticated during submission');
      await router.push('/login');
      return;
    }

    if (isEditMode.value) {
      const updateRequest: NoticeUpdate = {
        id: Number(route.params.id),
        title: formData.title,
        content: formData.content,
      };
      await updateNotice(updateRequest);
      // 수정 완료 후 해당 글의 상세 페이지로 이동
      await router.push(`/notices/${route.params.id}`);
    } else {
      const noticeRequest: NoticeRequest = {
        title: formData.title,
        content: formData.content,
      };
      const newNoticeId = await createNotice(noticeRequest);
      // 새 글 작성 완료 후 해당 글의 상세 페이지로 이동
      await router.push(`/notices/${newNoticeId}`);
    }
  } catch (error) {
    console.error(`Failed to ${isEditMode.value ? 'update' : 'create'} notice:`, error);
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
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <component
        :is="isMobile ? BasicMobilePostForm : BasicWebPostForm"
        type="notice"
        :initial-data="initialFormData"
        :submit-button-text="isEditMode ? '수정' : '작성'"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />
    </div>
  </div>
</template>
