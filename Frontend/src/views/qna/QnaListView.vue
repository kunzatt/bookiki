<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
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
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <div class="mb-4 flex justify-end">
        <BasicButton
          text="문의 작성"
          :is-enabled="authStore.isAuthenticated"
          size="M"
          @click="handleCreateClick"
        />
      </div>

      <QnaList @delete="showDeleteConfirm" />

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
  </div>
</template>
