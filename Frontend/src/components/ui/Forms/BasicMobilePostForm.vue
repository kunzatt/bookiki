<script setup lang="ts">
import BasicPostForm from './BasicPostForm.vue';
import type { PostType } from '@/types/common/postForm';

interface MobilePostFormProps {
  type: PostType;
  categories?: string[];
}

const mobilePostFormProps = defineProps<MobilePostFormProps>();

const handleSubmit = (formData: { title: string; content: string; category?: string }) => {
  console.log('Mobile form submitted:', formData);
  // 모바일용 제출 로직
};

const handleCancel = () => {
  console.log('Mobile form cancelled');
  // 모바일용 취소 로직
};
</script>

<template>
  <div class="w-full min-h-screen bg-white">
    <!-- 폼 내용 -->
    <div class="w-full max-w-md mx-auto p-4">
      <BasicPostForm
        :type="type"
        :categories="categories"
        titlePlaceholder="제목을 입력해주세요"
        contentPlaceholder="내용을 입력해주세요"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />
    </div>
  </div>
</template>