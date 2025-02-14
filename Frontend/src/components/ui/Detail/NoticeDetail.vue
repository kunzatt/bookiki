<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { QnaDetailResponse, QnaCommentRequest } from '@/types/api/qna';
import { selectQnaById, deleteQna, createQnaComment } from '@/api/qna';
import BasicButton from '../Button/BasicButton.vue';
import { formatDateTime } from '@/types/functions/dateFormats';
import { useAuthStore } from '@/stores/auth';
import ConfirmModal from '../Modal/ConfirmModal.vue';
import { marked } from 'marked';
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';
import { QnaTypeDescriptions } from '@/types/enums/qnaType';

const props = defineProps<{
  qnaId: number;
}>();

const authStore = useAuthStore();
const isAdmin = ref(false);
const router = useRouter();
const qna = ref<QnaDetailResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const showDeleteModal = ref(false);
const newComment = ref('');

const handleEdit = () => {
  router.push(`/qnas/${props.qnaId}/edit`);
};

const openDeleteModal = () => {
  showDeleteModal.value = true;
};

const handleDelete = async () => {
  try {
    await deleteQna(props.qnaId);
    await router.push('/qnas');
  } catch (err) {
    error.value = '문의사항 삭제 중 오류가 발생했습니다.';
  }
};

const handleCommentSubmit = async () => {
  if (!newComment.value.trim()) return;
  
  try {
    const request: QnaCommentRequest = {
      qnaId: props.qnaId,
      content: newComment.value
    };
    await createQnaComment(request);
    qna.value = await selectQnaById(props.qnaId);
    newComment.value = '';
  } catch (err) {
    error.value = '답변 등록 중 오류가 발생했습니다.';
  }
};

const getRenderedContent = (content: string) => {
  return marked(content);
};

onMounted(async () => {
  try {
    isAdmin.value = authStore.userRole === 'ADMIN';
    qna.value = await selectQnaById(props.qnaId);
  } catch (err) {
    error.value = '문의사항을 불러오는 중 오류가 발생했습니다.';
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
  <div v-else-if="qna" class="bg-white rounded-lg shadow-sm p-6">
    <!-- 제목 영역 -->
    <div class="border-b pb-4">
      <div class="flex items-center gap-2 mb-2">
        <BasicStatusBadge
          :text="QnaTypeDescriptions[qna.qnaType]"
          :type="getStatusType(qna.qnaType)"
          :isEnabled="true"
        />
        <h1 class="text-2xl font-bold text-gray-900">{{ qna.title }}</h1>
      </div>
      <div class="mt-2 flex items-center text-sm text-gray-500">
        <span>{{ qna.authorName }}</span>
        <span class="mx-2">|</span>
        <span>{{ formatDateTime(qna.createdAt) }}</span>
      </div>
    </div>

    <!-- 내용 영역 -->
    <div
      class="py-6 min-h-[200px] prose prose-sm max-w-none text-gray-700"
      v-html="getRenderedContent(qna.content)"
    />

    <!-- 답변 영역 -->
    <div v-if="qna.comments.length > 0" class="mt-6 border-t pt-4">
      <h2 class="text-lg font-semibold mb-4">답변</h2>
      <div v-for="comment in qna.comments" :key="comment.id" class="bg-gray-50 p-4 rounded-lg mb-4">
        <div class="flex justify-between items-center mb-2">
          <span class="font-medium">관리자</span>
          <span class="text-sm text-gray-500">{{ formatDateTime(comment.createdAt) }}</span>
        </div>
        <p class="text-gray-700">{{ comment.content }}</p>
      </div>
    </div>

    <!-- 관리자용 답변 작성 폼 -->
    <div v-if="isAdmin" class="mt-6 border-t pt-4">
      <h2 class="text-lg font-semibold mb-4">답변 작성</h2>
      <div class="flex flex-col gap-2">
        <textarea
          v-model="newComment"
          class="w-full p-2 border rounded-lg resize-none h-32"
          placeholder="답변을 입력하세요..."
        />
        <div class="flex justify-end">
          <BasicButton size="S" text="답변 등록" @click="handleCommentSubmit" />
        </div>
      </div>
    </div>

    <!-- 버튼 영역 -->
    <div class="flex justify-end gap-2 pt-4 border-t mt-6">
      <BasicButton size="S" :is-enabled="false" text="목록" @click="router.push('/qnas')" />
      <template v-if="authStore.userId === qna.authorId || isAdmin">
        <BasicButton size="S" text="수정" @click="handleEdit" />
        <BasicButton size="S" text="삭제" @click="openDeleteModal" />
      </template>
    </div>
  </div>

  <ConfirmModal
    v-model="showDeleteModal"
    title="문의사항 삭제"
    content="정말 삭제하시겠습니까?"
    confirm-text="삭제"
    cancel-text="취소"
    icon="delete"
    @confirm="handleDelete"
  />
</template>