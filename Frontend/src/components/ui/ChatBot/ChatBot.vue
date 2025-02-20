<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { sendMessage, sendFeedback } from '@/api/chatBot';
import type { Message } from '@/types/common/chatbot';
import { FeedbackCategory } from '@/types/enums/feedbackCategory';
import BasicSelect from '../Select/BasicSelect.vue';
import type { SelectOption } from '@/types/common/select';

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'close'): void;
}>();

const messages = ref<Message[]>([]);
const loading = ref(false);
const inputMessage = ref('');
const showFeedbackModal = ref(false);
const showFeedbackInputModal = ref(false);
const currentMessageIndex = ref(-1);
const messagesContainer = ref<HTMLElement | null>(null);
const feedbackContent = ref('');

// 피드백 카테고리 목록
const feedbackCategories: SelectOption[] = [
  { value: FeedbackCategory.QR_ERROR, label: 'QR 코드 오류' },
  { value: FeedbackCategory.LED_ERROR, label: 'LED 위치 표시 오류' },
  { value: FeedbackCategory.CAMERA_ERROR, label: '카메라 인식 오류' },
  { value: FeedbackCategory.BOOK_LOCATION, label: '도서 위치 오류' },
  { value: FeedbackCategory.SYSTEM_ERROR, label: '시스템 오류' },
  { value: FeedbackCategory.CHATBOT_ERROR, label: '챗봇 응답 오류' },
  { value: FeedbackCategory.OTHER, label: '기타' },
];

// 초기 카테고리 값 설정
const selectedCategory = ref<string>(FeedbackCategory.OTHER);

// 스크롤 처리
const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 메시지 처리
const handleSend = async (message: string = inputMessage.value) => {
  if (!message.trim() || loading.value) return;

  messages.value.push({
    type: 'user',
    content: message,
  });

  inputMessage.value = '';
  loading.value = true;

  try {
    const response = await sendMessage({ message });
    messages.value.push({
      type: 'bot',
      content: response.message,
      showFeedback: response.showAdminInquiryButton,
    });
    await scrollToBottom();
  } catch (error) {
    console.error('메시지 전송 실패:', error);
    messages.value.push({
      type: 'system',
      content: '죄송합니다. 일시적인 오류가 발생했습니다.',
    });
  } finally {
    loading.value = false;
  }
};

// 피드백 처리 - 수정된 부분
const handleFeedbackChoice = (messageId: number) => {
  currentMessageIndex.value = messageId;
  showFeedbackInputModal.value = true; // 바로 피드백 입력 모달 표시
};

const submitFeedbackContent = async () => {
  if (!feedbackContent.value.trim() || !selectedCategory.value) return;

  try {
    // 메시지 배열 전체를 새로 할당하여 반응성 트리거
    messages.value = messages.value.map((msg, index) => {
      if (index === currentMessageIndex.value) {
        return {
          ...msg,
          showFeedback: false,
        };
      }
      return msg;
    });

    await sendFeedback({
      originalIntent: messages.value[currentMessageIndex.value].content,
      feedbackMessage: feedbackContent.value,
      category: selectedCategory.value,
    });

    messages.value.push({
      type: 'system',
      content: '피드백이 접수되었습니다. 관리자 확인 후 조치하겠습니다.',
    });

    showFeedbackInputModal.value = false;
    feedbackContent.value = '';
    selectedCategory.value = FeedbackCategory.OTHER;
  } catch (error) {
    console.error('피드백 전송 실패:', error);
    messages.value.push({
      type: 'system',
      content: '피드백 전송에 실패했습니다. 다시 시도해 주세요.',
    });
  }
};

const handleClose = () => {
  emit('update:modelValue', false);
  emit('close');
};

onMounted(() => {
  messages.value.push({
    type: 'bot',
    content: '안녕하세요! Bookiki 도우미입니다. 무엇을 도와드릴까요?',
    showFeedback: false,
  });
});

watch(() => messages.value.length, scrollToBottom);
</script>

<template>
  <Teleport to="body">
    <!-- 반투명 오버레이 -->
    <div
      v-if="modelValue"
      class="fixed inset-0 bg-black bg-opacity-30 z-40"
      @click="handleClose"
    ></div>

    <!-- 챗봇 모달 -->
    <div
      v-if="modelValue"
      class="fixed bottom-5 right-5 w-[380px] bg-white rounded-lg shadow-xl z-50 flex flex-col max-h-[80vh]"
      @click.stop
    >
      <!-- 헤더 -->
      <div class="bg-[#698469] text-white p-4 rounded-t-lg flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="material-icons">support_agent</span>
          <h3 class="text-lg font-semibold">Bookiki 도우미</h3>
        </div>
        <button class="text-white" @click="handleClose">
          <span class="material-icons">close</span>
        </button>
      </div>

      <!-- 메시지 영역 -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 min-h-[300px] max-h-[400px]">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['mb-4', message.type === 'user' ? 'text-right' : 'text-left']"
        >
          <div
            :class="[
              'inline-block p-3 rounded-lg max-w-[80%]',
              message.type === 'user'
                ? 'bg-[#698469] text-white'
                : message.type === 'system'
                  ? 'bg-gray-200 text-gray-800'
                  : 'bg-[#F8F9F8] text-gray-800 border border-[#698469]',
            ]"
            style="white-space: pre-line"
          >
            {{ message.content }}
          </div>

          <!-- 피드백 옵션 -->
          <div v-if="message.showFeedback" class="mt-2">
            <button
              class="text-sm text-[#698469] px-3 py-1 rounded border border-[#698469] hover:bg-[#F8F9F8]"
              @click="handleFeedbackChoice(index)"
            >
              오류 신고 및 피드백 남기기
            </button>
          </div>
        </div>

        <!-- 로딩 표시 -->
        <div v-if="loading" class="flex justify-start">
          <div class="bg-[#F8F9F8] text-gray-800 p-3 rounded-lg inline-flex items-center gap-2">
            <span class="material-icons animate-spin">sync</span>
            응답 중...
          </div>
        </div>
      </div>

      <!-- 입력 영역 -->
      <div class="border-t border-gray-200 p-4 flex gap-2 items-center">
        <input
          v-model="inputMessage"
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-[#698469]"
          placeholder="메시지를 입력하세요..."
          :disabled="loading"
          @keyup.enter="handleSend()"
        />
        <button
          class="px-4 py-2 bg-[#698469] text-white rounded-lg hover:bg-[#4F634F] disabled:opacity-50"
          :disabled="loading || !inputMessage.trim()"
          @click="handleSend()"
        >
          <span class="material-icons">send</span>
        </button>
      </div>
    </div>

    <!-- 피드백 입력 모달 -->
    <div
      v-if="showFeedbackInputModal"
      class="fixed inset-0 bg-black bg-opacity-30 z-[60] flex items-end justify-center"
      @click="showFeedbackInputModal = false"
    >
      <div
        class="w-[380px] bg-white rounded-lg shadow-xl mx-4 mb-24 md:fixed md:bottom-auto md:right-5 md:top-[60%] md:mx-0 md:mb-0"
        @click.stop
      >
        <div class="p-4 space-y-3">
          <h3 class="text-base font-semibold">오류 신고 및 피드백</h3>

          <!-- 카테고리 선택 -->
          <div class="space-y-1">
            <label class="block text-sm font-medium text-gray-700">피드백 유형</label>
            <BasicSelect v-model="selectedCategory" :options="feedbackCategories" size="M" />
          </div>

          <!-- 피드백 내용 입력 -->
          <div class="space-y-1">
            <label class="block text-sm font-medium text-gray-700">피드백 내용</label>
            <textarea
              v-model="feedbackContent"
              rows="3"
              class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:border-[#698469]"
              placeholder="오류 신고 및 피드백 내용을 입력해주세요."
            ></textarea>
          </div>

          <!-- 버튼 -->
          <div class="flex justify-end gap-2 pt-2">
            <button
              class="px-3 h-10 text-sm text-gray-600 hover:bg-gray-100 rounded-lg"
              @click="showFeedbackInputModal = false"
            >
              취소
            </button>
            <button
              class="px-3 h-10 text-sm bg-[#698469] text-white rounded-lg hover:bg-[#4F634F] disabled:opacity-50"
              :disabled="!selectedCategory || !feedbackContent.trim()"
              @click="submitFeedbackContent"
            >
              전송
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.material-icons {
  font-size: 24px;
  line-height: 1;
}

/* 모바일 기본 폰트 크기 */
:deep(*) {
  font-size: 14px;
}

/* md 브레이크포인트(768px) 이상에서는 16px로 증가 */
@media (min-width: 768px) {
  :deep(*) {
    font-size: 16px;
  }
}

@media (max-width: 500px) {
  .fixed:not(.bg-black) {
    /* 챗봇 모달 크기 조정 */
    width: calc(100% - 32px) !important;
    right: 16px !important;
    left: 16px !important;
    margin: 0 auto;
    max-width: 380px;
    bottom: env(safe-area-inset-bottom, 16px) !important;
    padding-bottom: env(safe-area-inset-bottom, 0);
  }
}
</style>
