<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import BasicButton from '../Button/BasicButton.vue';
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';
import type { PostType } from '@/types/common/postForm';
import { QnaType, QnaTypeDescriptions } from '@/types/enums/qnaType';
import { marked } from 'marked'; // marked 라이브러리 추가 필요

interface PostFormProps {
  type: PostType;
  categories?: string[];
  titlePlaceholder?: string;
  contentPlaceholder?: string;
  initialData?: {
    title: string;
    content: string;
    category?: string;
  };
  submitButtonText?: string;
}

const props = withDefaults(defineProps<PostFormProps>(), {
  titlePlaceholder: '제목을 입력하세요',
  contentPlaceholder:
    '내용을 입력하세요 (마크다운 문법 지원)\n\n# 제목1\n## 제목2\n**굵게**\n*기울임*\n- 목록1\n- 목록2',
  submitButtonText: '작성',
});

const emit = defineEmits<{
  (e: 'submit', data: { title: string; content: string; category?: string }): void;
  (e: 'cancel'): void;
}>();

const title = ref('');
const content = ref('');
const selectedQnaType = ref<QnaType>(QnaType.NORMAL);
const isPreview = ref(false);

// 마크다운 변환된 내용
const renderedContent = computed(() => {
  return marked(content.value);
});

watch(
  () => props.initialData,
  (newData) => {
    if (newData) {
      title.value = newData.title;
      content.value = newData.content;
      if (newData.qnaType) {
        selectedQnaType.value = newData.qnaType;
      }
    }
  },
  { immediate: true },
);

const handleSubmit = async (e: Event) => {
  e.preventDefault();
  const formData = {
    title: title.value,
    content: content.value,
    qnaType: props.type === 'qna' ? selectedQnaType.value : undefined
  };

  emit('submit', formData);
};

const handleCancel = () => {
  emit('cancel');
};

const togglePreview = () => {
  isPreview.value = !isPreview.value;
};
</script>

<template>
  <form @submit="handleSubmit" class="w-full" method="POST">
    <div class="w-full">
      <!-- 카테고리 부분 -->
      <div v-if="type === 'qna'" class="mb-4 flex flex-wrap gap-2">
        <BasicStatusBadge
          v-for="[type, description] in Object.entries(QnaTypeDescriptions)"
          :key="type"
          :text="description"
          :type="selectedQnaType === type ? 'primary' : 'gray'"
          :isEnabled="selectedQnaType === type"
          class="cursor-pointer"
          @click="selectedQnaType = type as QnaType"
        />
      </div>

      <!-- 제목 입력 -->
      <input
        v-model="title"
        type="text"
        :placeholder="titlePlaceholder"
        class="w-full px-4 py-2 border border-gray-200 rounded-lg mb-4 focus:outline-none focus:border-[#698469]"
        required
      />

      <!-- 내용 입력 영역 -->
      <div class="min-h-[200px] mb-4 border border-gray-200 rounded-lg">
        <div class="flex items-center p-2 border-b border-gray-200">
          <button
            type="button"
            class="px-3 py-1 text-sm rounded"
            :class="isPreview ? 'text-gray-600' : 'bg-gray-100 text-gray-800'"
            @click="togglePreview"
          >
            작성
          </button>
          <button
            type="button"
            class="px-3 py-1 text-sm rounded ml-2"
            :class="isPreview ? 'bg-gray-100 text-gray-800' : 'text-gray-600'"
            @click="togglePreview"
          >
            미리보기
          </button>
        </div>

        <!-- 작성 모드 -->
        <textarea
          v-if="!isPreview"
          v-model="content"
          :placeholder="contentPlaceholder"
          class="w-full h-[160px] p-4 resize-none focus:outline-none"
          required
        />

        <!-- 미리보기 모드 -->
        <div
          v-else
          class="w-full h-[160px] p-4 overflow-y-auto prose prose-sm max-w-none"
          v-html="renderedContent"
        />
      </div>

      <!-- 버튼 영역 -->
      <div class="flex justify-end gap-2">
        <BasicButton type="button" size="M" text="취소" :isEnabled="false" @click="handleCancel" />
        <BasicButton type="submit" size="M" :text="submitButtonText" :isEnabled="true" />
      </div>
    </div>
  </form>
</template>

<style scoped>
:deep(.prose) {
  max-width: none;
}
</style>
