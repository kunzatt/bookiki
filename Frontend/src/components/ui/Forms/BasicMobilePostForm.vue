<script setup lang="ts">
import BasicPostForm from './BasicPostForm.vue'
import type { PostType } from '@/types/common/postForm'

interface MobilePostFormProps {
  type: PostType
  categories?: string[]
}

const props = defineProps<MobilePostFormProps>()

const emit = defineEmits(['submit', 'cancel'])

const handleSubmit = (formData: { title: string; content: string; category?: string }) => {
  // 모바일용 제출 로직
  emit('submit', formData)
  console.log('Mobile form submitted:', formData)
}

const handleCancel = () => {
  // 모바일용 취소 로직
  emit('cancel')
  console.log('Mobile form cancelled')
}
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
