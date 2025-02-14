<script setup lang="ts">
import BasicPostForm from './BasicPostForm.vue';
import type { PostType } from '@/types/common/postForm';

interface WebPostFormProps {
  type: PostType;
  categories?: string[];
  initialData?: {
    title: string;
    content: string;
    category?: string;
  };
  submitButtonText?: string; // 추가
}

const props = defineProps<WebPostFormProps>();
const emit = defineEmits(['submit', 'cancel']);

const handleSubmit = (formData: { title: string; content: string; category?: string }) => {
  emit('submit', formData);
  console.log('Web form submitted:', formData);
};

const handleCancel = () => {
  emit('cancel');
  console.log('Web form cancelled');
};
</script>

<template>
  <div class="max-w-4xl mx-auto p-8">
    <h1 class="text-2xl font-bold mb-6">
      {{ type === 'notice' ? '공지사항 작성' : '문의하기' }}
    </h1>
    <BasicPostForm
      :type="type"
      :categories="categories"
      :initial-data="initialData"
      :submit-button-text="submitButtonText"
      titlePlaceholder="제목을 입력해주세요"
      contentPlaceholder="내용을 입력해주세요"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>
