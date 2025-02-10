<!-- components/BookCard.vue -->
<script setup lang="ts">
import { type BookCardProps } from './BookCard'
import { computed } from 'vue';

const props = defineProps<BookCardProps>()

// 추가 정보 표시를 위한 computed 속성
const additionalInfo = computed(() => {
  switch (props.variant) {
    case 'returnDueDate':
      return props.returnDueDate ? `${props.returnDueDate} 까지` : ''
    case 'expectedReturnDate':
      return props.expectedReturnDate ? `${props.expectedReturnDate} 반납 예정` : ''
    case 'author':
      return props.author || ''
    case 'borrowCount':
      return props.borrowCount ? `${props.borrowCount}회 대출` : ''
    default:
      return ''
  }
})

const fullImageUrl = computed(() => {
  if (props.coverImage.startsWith('http')) {
    return props.coverImage  // 이미 전체 URL인 경우
  }
  return `${import.meta.env.VITE_API_BASE_URL}${props.coverImage}`  // 경로만 있는 경우
})
</script>

<template>
    <div class="flex flex-col bg-white rounded-lg overflow-hidden w-64">
      <!-- 책 표지 이미지 -->
      <div class="h-80 overflow-hidden">
        <img 
          :src="fullImageUrl" 
          :alt="title"
          class="w-full h-full object-cover"
        >
      </div>
      
      <!-- 책 정보 -->
      <div class="p-4 flex flex-col gap-2">
        <h3 class="text-lg font-semibold line-clamp-2">{{ title }}</h3>
        <p 
          class="text-sm text-gray-600"
        >
          {{ additionalInfo }}
        </p>
      </div>
    </div>
  </template>