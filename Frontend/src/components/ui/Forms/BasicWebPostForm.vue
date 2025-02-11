<script setup lang="ts">
import BasicPostForm from './BasicPostForm.vue';
import type { PostType } from '@/types/common/postForm';

interface WebPostFormProps {
  type: PostType;
  categories?: string[];
}

const webPostFormProps = defineProps<WebPostFormProps>();

const handleSubmit = (formData: { title: string; content: string; category?: string }) => {
  console.log('Web form submitted:', formData);
  // 웹용 제출 로직
};

const handleCancel = () => {
  console.log('Web form cancelled');
  // 웹용 취소 로직
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
      titlePlaceholder="제목을 입력해주세요"
      contentPlaceholder="내용을 입력해주세요"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>