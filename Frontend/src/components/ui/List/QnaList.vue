<script setup lang="ts">
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';
import type { QnaListResponse } from '@/types/api/qna';
import { QnaType, QnaTypeDescriptions  } from '@/types/enums/qnaType';
import type { BadgeStatus } from '@/types/common/ui';

interface QnaProps {
  items: QnaListResponse[];
}

const qnaProps = defineProps<QnaProps>();

// 날짜 포맷팅 함수 (YY.MM.DD)
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${String(date.getFullYear()).slice(2)}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};

// 상태에 따른 배지 타입 매핑
const getStatusType = (qna_type: QnaType): BadgeStatus => {
  const typeMap: Record<QnaType, BadgeStatus> = {
    [QnaType.NORMAL]: 'info',
    [QnaType.NEW_BOOK]: 'success',
    [QnaType.CHANGE_INFO]: 'warning'
  };
  return typeMap[qna_type] || 'gray';
};

// QnaTypeDescriptions를 사용하여 표시 텍스트 가져오기
const getDisplayText = (qna_type: QnaType): string => {
  return QnaTypeDescriptions[qna_type];
};

</script>

<template>
  <div class="w-full max-w-md mx-auto">
    <ul class="space-y-4">
      <li 
        v-for="(item, index) in items" 
        :key="index"
        class="bg-white p-4 rounded-lg shadow-sm"
      >
        <div class="flex justify-between items-start mb-2">
          <!-- 상태 배지 -->
          <BasicStatusBadge
            :text="item.qnaType"
            :type="getStatusType(item.qnaType)"
            :isEnabled="true"
          />
          <!-- 날짜 -->
          <span class="text-sm text-gray-500">
            {{ formatDate(item.createdAt) }}
          </span>
        </div>
        
        <!-- 제목과 작성자 정보 -->
        <div class="mt-2">
          <h3 class="text-base font-medium text-gray-900 mb-1">
            {{ item.title }}
          </h3>
          <p class="text-sm text-gray-500">
            {{ item.quthorName }}
          </p>
        </div>
      </li>
    </ul>
  </div>
</template>