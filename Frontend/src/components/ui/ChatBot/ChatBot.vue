<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { sendMessage, sendFeedback } from '@/api/chatBot';
import type { Message } from '@/types/common/chatbot';

const props = defineProps<{
  modelValue: boolean; // v-model을 위해 modelValue로 변경
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'close'): void;
}>();

const messages = ref<Message[]>([]);
const loading = ref(false);
const inputMessage = ref('');
const showFeedbackModal = ref(false);
const showReportModal = ref(false);
const currentMessageIndex = ref(-1);
const reportContent = ref('');
const messagesContainer = ref<HTMLElement | null>(null);

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

// 피드백 처리
const handleFeedbackChoice = (messageId: number) => {
  currentMessageIndex.value = messageId;
  showFeedbackModal.value = true;
};

const submitFeedback = async (isContinueChat: boolean) => {
  const targetMessage = messages.value[currentMessageIndex.value];
  if (!targetMessage || targetMessage.type !== 'bot') return;

  try {
    if (!isContinueChat) {
      await sendFeedback({
        originalIntent: targetMessage.content,
        feedbackMessage: '피드백을 남기겠습니다.',
        category: 'FEEDBACK',
      });

      messages.value.push({
        type: 'system',
        content: '피드백을 남겨주세요.',
        showReportButton: true,
      });
    }

    targetMessage.showFeedback = false;
    showFeedbackModal.value = false;
  } catch (error) {
    console.error('피드백 처리 실패:', error);
    messages.value.push({
      type: 'system',
      content: '피드백 처리에 실패했습니다. 다시 시도해 주세요.',
    });
  }
};

// 신고 처리
const handleReport = async () => {
  const targetMessage = messages.value[currentMessageIndex.value];
  if (!targetMessage || targetMessage.type !== 'bot') return;

  try {
    await sendFeedback({
      originalIntent: targetMessage.content,
      feedbackMessage: reportContent.value,
      category: 'ISSUE_REPORT',
    });

    messages.value.push({
      type: 'system',
      content: '신고가 접수되었습니다. 관리자 확인 후 조치하겠습니다.',
    });

    showReportModal.value = false;
    reportContent.value = '';
  } catch (error) {
    console.error('신고 전송 실패:', error);
    messages.value.push({
      type: 'system',
      content: '신고 접수에 실패했습니다. 다시 시도해 주세요.',
    });
  }
};

const handleClose = () => {
  emit('update:modelValue', false);
  emit('close');
};

// 초기 메시지 설정
onMounted(() => {
  messages.value.push({
    type: 'bot',
    content: '안녕하세요! Bookiki 도우미입니다. 무엇을 도와드릴까요?',
    showFeedback: false,
  });
});

// 메시지 추가될 때마다 스크롤
watch(() => messages.value.length, scrollToBottom);
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="(val) => emit('update:modelValue', val)"
    :show-close="false"
    :modal="true"
    :close-on-click-modal="false"
    width="450px"
    class="chatbot-dialog"
  >
    <template #header>
      <div
        class="bg-[#698469] text-white p-4 -mt-6 -mx-4 rounded-t-lg flex items-center justify-between"
      >
        <div class="flex items-center gap-2">
          <span class="material-icons">support_agent</span>
          <h3 class="text-lg font-semibold">Bookiki 도우미</h3>
        </div>
        <button class="text-white" @click="handleClose">
          <span class="material-icons">close</span>
        </button>
      </div>
    </template>

    <!-- Messages -->
    <div ref="messagesContainer" class="h-[400px] overflow-y-auto px-2">
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
            답변에 대한 피드백 남기기
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-start">
        <div class="bg-[#F8F9F8] text-gray-800 p-3 rounded-lg inline-flex items-center gap-2">
          <span class="material-icons animate-spin">sync</span>
          응답 중...
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="border-t border-gray-200 pt-4 px-4 flex gap-2 items-center">
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

    <!-- 피드백 모달 -->
    <el-dialog v-model="showFeedbackModal" title="답변에 대한 피드백" width="400px" append-to-body>
      <div class="space-y-4">
        <button
          class="w-full p-3 text-[#698469] border border-[#698469] rounded-lg hover:bg-[#F8F9F8]"
          @click="submitFeedback(false)"
        >
          피드백/신고 남기기
        </button>
        <button
          class="w-full p-3 bg-[#698469] text-white rounded-lg hover:bg-[#4F634F]"
          @click="submitFeedback(true)"
        >
          챗봇에게 이어서 문의하기
        </button>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.chatbot-dialog :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

.chatbot-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.chatbot-dialog :deep(.el-dialog__body) {
  padding: 1rem;
}

.chatbot-dialog :deep(.el-dialog__footer) {
  padding: 0;
  margin: 0;
  height: auto;
}
</style>
