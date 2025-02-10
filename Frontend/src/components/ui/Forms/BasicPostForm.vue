<script setup lang="ts">
import BasicButton from '../Button/BasicButton.vue';
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';
import { ref } from 'vue';

interface Props {
  /**
   * 폼 타입 (notice: 공지사항, inquiry: 문의사항)
   */
  type: 'notice' | 'inquiry';
  /**
   * 문의사항 카테고리 목록 (type이 inquiry일 때만 사용)
   */
  categories?: string[];
  /**
   * 제목 placeholder
   * @default '제목을 입력하세요'
   */
  titlePlaceholder?: string;
  /**
   * 내용 placeholder
   * @default '내용을 입력하세요'
   */
  contentPlaceholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  titlePlaceholder: '제목을 입력하세요',
  contentPlaceholder: '내용을 입력하세요',
  categories: () => []
});

const emit = defineEmits<{
  (e: 'submit', data: { title: string; content: string; category?: string }): void;
  (e: 'cancel'): void;
}>();

const title = ref('');
const content = ref('');
const selectedCategory = ref('');

const handleSubmit = () => {
  const formData = {
    title: title.value,
    content: content.value,
    ...(props.type === 'inquiry' && { category: selectedCategory.value })
  };
  emit('submit', formData);
};
</script>

<template>
  <div class="w-full">
    <!-- 카테고리 선택 (문의사항일 때만) -->
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

    <!-- 제목 입력 -->
    <input
      v-model="title"
      type="text"
      :placeholder="titlePlaceholder"
      class="w-full px-4 py-2 border border-gray-200 rounded-lg mb-4 focus:outline-none focus:border-[#698469]"
    />

    <!-- 내용 입력 -->
    <div class="min-h-[200px] mb-4 border border-gray-200 rounded-lg">
      <!-- 에디터 툴바 -->
      <div class="flex items-center gap-2 p-2 border-b border-gray-200">
        <button 
          v-for="icon in [
            'format_bold',
            'format_italic',
            'format_underline',
            'format_list_bulleted',
            'format_list_numbered'
          ]" 
          :key="icon"
          class="p-1 hover:bg-gray-100 rounded"
        >
          <i class="material-icons text-gray-600 text-lg">{{ icon }}</i>
        </button>
      </div>
      
      <!-- 에디터 본문 -->
      <textarea
        v-model="content"
        :placeholder="contentPlaceholder"
        class="w-full h-[160px] p-4 resize-none focus:outline-none"
      />
    </div>

    <!-- 버튼 영역 -->
    <div class="flex justify-end gap-2">
      <BasicButton
        size="M"
        text="취소"
        :isEnabled="false"
        @click="emit('cancel')"
      />
      <BasicButton
        size="M"
        text="작성"
        :isEnabled="true"
        @click="handleSubmit"
      />
    </div>
  </div>
</template>