<script setup lang="ts">
import { ref } from 'vue';
import BasicButton from '../Button/BasicButton.vue';
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';
import type { PostType } from '@/types/common/postForm';

interface PostFormProps {
  type: PostType;
  categories?: string[];
  titlePlaceholder?: string;
  contentPlaceholder?: string;
}

const props = withDefaults(defineProps<PostFormProps>(), {
  titlePlaceholder: '제목을 입력하세요',
  contentPlaceholder: '내용을 입력하세요',
});

const emit = defineEmits<{
  (e: 'submit', data: { title: string; content: string; category?: string }): void;
  (e: 'cancel'): void;
}>();

const title = ref('');
const content = ref('');
const selectedCategory = ref('');

const handleSubmit = async (e: Event) => {
  e.preventDefault(); // 기본 제출 동작 방지

  // 폼 데이터 준비
  const formData = {
    title: title.value,
    content: content.value,
    ...(props.type === 'inquiry' && { category: selectedCategory.value }),
  };

  console.log('Form submitted with:', formData);
  // 부모 컴포넌트로 데이터 전달
  emit('submit', formData);
};

const handleCancel = () => {
  emit('cancel');
};

const handleFormat = (command: string) => {
  document.execCommand(command, false);
};

// 명령어 매핑 함수
const getCommand = (icon: string) => {
  const commands = {
    format_bold: 'bold',
    format_italic: 'italic',
    format_underline: 'underline',
    format_list_bulleted: 'insertUnorderedList',
    format_list_numbered: 'insertOrderedList',
  };
  return commands[icon] || '';
};
</script>

<template>
  <form @submit="handleSubmit" class="w-full" method="POST">
    <div class="w-full">
      <div v-if="type === 'inquiry'" class="mb-4 flex flex-wrap gap-2">
        <BasicStatusBadge
          v-for="category in categories"
          :key="category"
          :text="category"
          :type="selectedCategory === category ? 'primary' : 'gray'"
          :isEnabled="selectedCategory === category"
          class="cursor-pointer"
          @click="selectedCategory = category"
        />
      </div>

      <input
        v-model="title"
        type="text"
        :placeholder="titlePlaceholder"
        class="w-full px-4 py-2 border border-gray-200 rounded-lg mb-4 focus:outline-none focus:border-[#698469]"
        required
      />

      <div class="min-h-[200px] mb-4 border border-gray-200 rounded-lg">
        <div class="flex items-center p-2 border-b border-gray-200 overflow-x-auto">
          <button
            v-for="icon in [
              'format_bold',
              'format_italic',
              'format_underline',
              'format_list_bulleted',
              'format_list_numbered',
            ]"
            :key="icon"
            type="button"
            class="p-1 hover:bg-gray-100 rounded flex-shrink-0 mr-1"
            @mousedown.prevent="handleFormat(getCommand(icon))"
          >
            <i class="material-icons text-gray-600 text-lg">{{ icon }}</i>
          </button>
        </div>

        <textarea
          v-model="content"
          :placeholder="contentPlaceholder"
          class="w-full h-[160px] p-4 resize-none focus:outline-none"
          required
        />
      </div>

      <div class="flex justify-end gap-2">
        <BasicButton type="button" size="M" text="취소" :isEnabled="false" @click="handleCancel" />
        <BasicButton type="submit" size="M" text="작성" :isEnabled="true" />
      </div>
    </div>
  </form>
</template>
