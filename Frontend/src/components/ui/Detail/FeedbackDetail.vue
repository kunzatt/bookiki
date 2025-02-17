<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { ChatbotFeedbackResponse, UpdateFeedbackStatusRequest } from '@/types/api/chatbot';
import { selectFeedbackById } from '@/api/chatBot';
import BasicButton from '../Button/BasicButton.vue';
import { formatDateTime } from '@/types/functions/dateFormats';
import { useAuthStore } from '@/stores/auth';
import { marked } from 'marked';
import { FeedbackCategory, FeedbackCategoryDescriptions } from '@/types/enums/feedbackCategory';
import { FeedbackStatus, FeedbackStatusDescriptions } from '@/types/enums/feedbackStatus';
import { updateFeedbackStatus } from '@/api/chatBot';

const props = defineProps<{
  feedbackId: number;
}>();

const authStore = useAuthStore();
const isAdmin = ref(false);
const router = useRouter();
const feedback = ref<ChatbotFeedbackResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const handleUpdateStatus = async (newStatus: FeedbackStatus) => {
  try {
    const request: UpdateFeedbackStatusRequest = {
      status: newStatus,
    };
    feedback.value = await updateFeedbackStatus(props.feedbackId, request);
  } catch (err) {
    error.value = '피드백 상태 수정 중 오류가 발생했습니다.';
  }
};

const getRenderedContent = (content: string) => {
  return marked(content);
};

onMounted(async () => {
  try {
    isAdmin.value = authStore.userRole === 'ADMIN';
    feedback.value = await selectFeedbackById(props.feedbackId);
  } catch (err) {
    error.value = '피드백을 불러오는 중 오류가 발생했습니다.';
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
  <div v-else-if="feedback" class="bg-white rounded-lg shadow-sm p-6">
    <!-- 버튼 영역 -->
    <div class="flex justify-end">
      <BasicButton text="목록" size="M" :isEnabled="true" @click="router.push('/feedbacks')" />
    </div>
    <!-- 피드백 헤더 영역 -->
    <div class="border-b pb-6 space-y-4">
      <!-- 피드백 유형 -->
      <div class="inline-block px-3 py-1 bg-gray-100 rounded-full text-gray-700">
        {{
          FeedbackCategoryDescriptions[feedback.category as FeedbackCategory] || feedback.category
        }}
      </div>

      <!-- 상태 표시 및 수정 영역 -->
      <div class="flex items-center gap-4">
        <span class="text-sm text-gray-600 font-medium">처리 상태:</span>
        <!-- 관리자가 아닐 경우 -->
        <span v-if="!isAdmin" class="px-3 py-1 bg-blue-50 text-blue-700 rounded-full">
          {{ FeedbackStatusDescriptions[feedback.status as FeedbackStatus] }}
        </span>
        <!-- 관리자일 경우 선택 가능한 드롭다운 -->
        <select
          v-else
          :value="feedback.status"
          @change="handleUpdateStatus($event.target.value as FeedbackStatus)"
          class="px-3 py-1 border rounded-full bg-white text-gray-700 cursor-pointer hover:border-blue-500 focus:outline-none focus:border-blue-500"
        >
          <option v-for="status in Object.values(FeedbackStatus)" :key="status" :value="status">
            {{ FeedbackStatusDescriptions[status] }}
          </option>
        </select>
      </div>

      <!-- 메타 정보 -->
      <div class="flex items-center space-x-4 text-sm text-gray-600">
        <div class="flex items-center">
          <span class="font-medium mr-2">작성자 ID:</span>
          <span>{{ feedback.userId }}</span>
        </div>
        <div class="w-px h-4 bg-gray-300"></div>
        <div class="flex items-center">
          <span class="font-medium mr-2">작성일:</span>
          <span>{{ formatDateTime(feedback.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 원본 의도 영역 (있는 경우에만 표시) -->
    <div v-if="feedback.originalIntent" class="mt-6">
      <h3 class="text-gray-700 font-medium mb-3">챗봇 메세지</h3>
      <div class="p-4 bg-gray-50 rounded-lg text-gray-600 text-sm whitespace-pre-line">
        {{ feedback.originalIntent }}
      </div>
    </div>

    <!-- 피드백 내용 영역 -->
    <div class="mt-6">
      <h3 class="text-gray-700 font-medium mb-3">사용자 피드백</h3>
      <div
        class="p-4 bg-gray-50 rounded-lg prose prose-sm max-w-none text-gray-700"
        v-html="getRenderedContent(feedback.feedbackMessage)"
      />
    </div>
  </div>
</template>
