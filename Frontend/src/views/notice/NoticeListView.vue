<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import NoticeList from '@/components/ui/List/NoticeList.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import ConfirmModal from '@/components/ui/Modal/ConfirmModal.vue';
import { useAuthStore } from '@/stores/auth';
import { deleteNotice } from '@/api/notice';

const router = useRouter();
const authStore = useAuthStore();

// 모달 관련 상태
const showDeleteModal = ref(false);
const deleteNoticeId = ref<number | null>(null);

// Handle notice creation
const handleCreateClick = () => {
  router.push('/notices/create');
};

// Show delete confirmation modal
const showDeleteConfirm = (id: number) => {
  deleteNoticeId.value = id;
  showDeleteModal.value = true;
};

// Handle notice deletion
const handleDelete = async () => {
  if (deleteNoticeId.value) {
    try {
      await deleteNotice(deleteNoticeId.value);
      // Refresh the list after deletion
      window.location.reload();
    } catch (error) {
      console.error('공지사항 삭제 실패:', error);
    }
  }
};
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <!-- Create Button (Admin only) -->
      <div v-if="authStore.userRole == 'ADMIN'" class="mb-4 flex justify-end">
        <BasicButton text="공지 작성" :is-enabled="true" size="M" @click="handleCreateClick" />
      </div>

      <!-- Notice List Component -->
      <NoticeList @delete="showDeleteConfirm" />
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <ConfirmModal
    v-model="showDeleteModal"
    title="공지사항 삭제"
    content="정말로 이 공지사항을 삭제하시겠습니까?"
    icon="delete"
    confirm-text="삭제"
    cancel-text="취소"
    @confirm="handleDelete"
  />
</template>
