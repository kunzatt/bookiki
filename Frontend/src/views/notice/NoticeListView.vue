<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
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
  <div class="min-h-screen flex">
    <!-- Sidebar (Desktop) -->
    <Sidebar class="hidden md:block fixed h-full" />

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col md:ml-64">
      <!-- Mobile Header -->
      <HeaderMobile
        class="md:hidden fixed top-0 left-0 right-0 z-10"
        title="공지사항"
        type="main"
      />

      <!-- Desktop Header -->
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="공지사항" />

      <!-- Scrollable Content Area -->
      <div class="flex-1 overflow-y-auto pt-16">
        <!-- Padding for header -->
        <div class="h-full p-4 md:p-8">
          <!-- Create Button (Admin only) -->
          <div v-if="authStore.userRole == 'ADMIN'" class="mb-4 flex justify-end">
            <BasicButton text="공지 작성" :is-enabled="true" size="M" @click="handleCreateClick" />
          </div>

          <!-- Notice List Component -->
          <NoticeList @delete="showDeleteConfirm" />
        </div>
      </div>

      <!-- Mobile Bottom Navigation -->
      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
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
  </div>
</template>
