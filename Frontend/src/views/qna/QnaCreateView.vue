<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
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
import { QnaType, QnaTypeDescriptions } from '@/types/enums/qnaType';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const isMobile = ref(false);
const isEditMode = ref(false);
const initialFormData = ref({ 
  title: '', 
  content: '',
  qnaType: QnaType.NORMAL 
});
const pageTitle = ref('문의사항 작성');

// QnaType 옵션 생성
const qnaCategories = Object.entries(QnaTypeDescriptions).map(([value, label]) => label);

onMounted(async () => {
  checkDeviceType();
  window.addEventListener('resize', checkDeviceType);

  authStore.initializeFromStorage();

  if (!authStore.isAuthenticated) {
    router.push('/login');
    return;
  }

  const qnaId = route.params.id;
  if (qnaId) {
    isEditMode.value = true;
    pageTitle.value = '문의사항 수정';
    try {
      const qna = await selectQnaById(Number(qnaId));
      if (authStore.userId !== qna.authorId && authStore.userRole !== 'ADMIN') {
        router.push('/qnas');
        return;
      }
      initialFormData.value = {
        title: qna.title,
        content: qna.content,
        qnaType: qna.qnaType
      };
    } catch (error) {
      console.error('Failed to load qna:', error);
      router.push('/qnas');
    }
  }
});

const categories = Object.entries(QnaTypeDescriptions).map(([type, label]) => ({
  type: type as QnaType,
  label
}));

const checkDeviceType = () => {
  isMobile.value = window.innerWidth < 768;
};

const handleSubmit = async (formData: { title: string; qnaType: QnaType; content: string }) => {
  try {
    if (!authStore.isAuthenticated) {
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
      await router.push(`/qnas/${route.params.id}`); // 수정된 부분
    } else {
      const qnaRequest: QnaRequest = {
        title: formData.title,
        qnaType: formData.qnaType,
        content: formData.content,
      };
      const newId = await createQna(qnaRequest);
      await router.push(`/qnas/${newId}`); // 생성된 글의 상세 페이지로 이동
    }
  } catch (error) {
    console.error(`Failed to ${isEditMode.value ? 'update' : 'create'} qna:`, error);
    if (error.response?.status === 401 || error.response?.status === 302) {
      await router.push('/login');
    }
  }
};

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
              :categories="qnaCategories"
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