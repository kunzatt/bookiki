<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { NoticeResponse } from '@/types/api/notice';
import { selectNoticeById, deleteNotice } from '@/api/notice';
import BasicButton from '../Button/BasicButton.vue';
import { formatDateTime } from '@/types/functions/dateFormats';
import { useAuthStore } from '@/stores/auth';
import ConfirmModal from '../Modal/ConfirmModal.vue';
import { marked } from 'marked';

const props = defineProps<{
  noticeId: number;
}>();

const authStore = useAuthStore();
const isAdmin = ref(false);
const router = useRouter();
const notice = ref<NoticeResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const showDeleteModal = ref(false);

const handleEdit = () => {
  router.push(`/notices/${props.noticeId}/edit`);
};

const openDeleteModal = () => {
  showDeleteModal.value = true;
};

const handleDelete = async () => {
  try {
    await deleteNotice(props.noticeId);
    await router.push('/notices');
  } catch (err) {
    error.value = '공지사항 삭제 중 오류가 발생했습니다.';
  }
};

const getRenderedContent = (content: string) => {
  return marked(content);
};

onMounted(async () => {
  try {
    isAdmin.value = authStore.userRole === 'ADMIN';
    notice.value = await selectNoticeById(props.noticeId);
  } catch (err) {
    error.value = '공지사항을 불러오는 중 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div v-if="loading" class="flex justify-center items-center h-64">
    <div class="text-gray-500">로딩 중...</div>
  </div>
  <div v-else-if="error" class="text-red-500 p-4">
    {{ error }}
  </div>
  <div v-else-if="notice" class="bg-white rounded-lg shadow-sm p-6">
    <!-- 제목 영역 -->
    <div class="border-b pb-4">
      <h1 class="text-2xl font-bold text-gray-900">{{ notice.title }}</h1>
      <div class="mt-2 flex items-center text-sm text-gray-500">
        <span>{{ formatDateTime(notice.createdAt) }}</span>
        <span class="mx-2">|</span>
        <span>조회수: {{ notice.viewCount }}</span>
      </div>
    </div>

    <!-- 내용 영역 -->
    <div
      class="py-6 min-h-[200px] prose prose-sm max-w-none text-gray-700"
      v-html="getRenderedContent(notice.content)"
    />

    <!-- 버튼 영역 -->
    <div class="flex justify-end gap-2 pt-4 border-t mt-6">
      <BasicButton size="S" :is-enabled="false" text="목록" @click="router.push('/notices')" />
      <template v-if="isAdmin">
        <BasicButton size="S" text="수정" @click="handleEdit" />
        <BasicButton size="S" text="삭제" @click="openDeleteModal" />
      </template>
    </div>
  </div>

  <ConfirmModal
    v-model="showDeleteModal"
    title="공지사항 삭제"
    content="정말 삭제하시겠습니까?"
    confirm-text="삭제"
    cancel-text="취소"
    icon="delete"
    @confirm="handleDelete"
  />
</template>
