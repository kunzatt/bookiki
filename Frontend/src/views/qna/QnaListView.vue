<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import ConfirmModal from '@/components/ui/Modal/ConfirmModal.vue';
import { useAuthStore } from '@/stores/auth';
import { deleteQna } from '@/api/qna';
import QnaList from '@/components/ui/List/QnaList.vue';

const router = useRouter();
const authStore = useAuthStore();

const showDeleteModal = ref(false);
const deleteQnaId = ref<number | null>(null);

const handleCreateClick = () => {
  router.push('/qnas/create');
};

const showDeleteConfirm = (id: number) => {
  deleteQnaId.value = id;
  showDeleteModal.value = true;
};

const handleDelete = async () => {
  if (deleteQnaId.value) {
    try {
      await deleteQna(deleteQnaId.value);
      window.location.reload();
    } catch (error) {
      console.error('문의사항 삭제 실패:', error);
    }
  }
};
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
          <!-- 일반 사용자도 작성할 수 있도록 변경 -->
          <div class="mb-4 flex justify-end">
            <BasicButton
              text="문의 작성"
              :is-enabled="authStore.isAuthenticated"
              size="M"
              @click="handleCreateClick"
            />
          </div>

          <QnaList @delete="showDeleteConfirm" />
        </div>
      </div>

      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>

    <ConfirmModal
      v-model="showDeleteModal"
      title="문의사항 삭제"
      content="정말로 이 문의사항을 삭제하시겠습니까?"
      icon="delete"
      confirm-text="삭제"
      cancel-text="취소"
      @confirm="handleDelete"
    />
  </div>
</template>