<script setup lang="ts">
import BasicStatusBadge from '../Badge/BasicStatusBadge.vue';

interface QnaItem {
  id: number;         // string -> number로 수정
  title: string;      
  qna_type: string;
  content: string;
  author_id: number;
  created_at: string;
  updated_at: string;
  deleted: number;
}

interface Props {
  items: QnaItem[];
}

const props = defineProps<Props>();

// 날짜 포맷팅 함수 (YY.MM.DD)
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${String(date.getFullYear()).slice(2)}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};

// 상태에 따른 배지 타입 매핑
const getStatusType = (qna_type: string) => {
  const typeMap = {
    '질문': 'primary',
    '회망도서 신청': 'info',
    '이름/사번 변경': 'warning'
  };
  return typeMap[qna_type] || 'gray';
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
            :text="item.qna_type"
            :type="getStatusType(item.qna_type)"
            :isEnabled="true"
          />
          <!-- 날짜 -->
          <span class="text-sm text-gray-500">
            {{ formatDate(item.date) }}
          </span>
        </div>
        
        <!-- 제목과 작성자 정보 -->
        <div class="mt-2">
          <h3 class="text-base font-medium text-gray-900 mb-1">
            {{ item.title }}
          </h3>
          <p class="text-sm text-gray-500">
            {{ item.author }}
          </p>
        </div>
      </li>
    </ul>
  </div>
</template>

<!-- 사용 예시
<script setup>
const qnaItems = [
  {
    title: "도커, 컨테이너 빌드업!",
    qna_type: "질문",
    author: "박성문",
    date: "2025-02-09"
  },
  {
    title: "도커, 컨테이너 빌드업!",
    qna_type: "회망도서 신청",
    author: "박성문",
    date: "2025-02-10"
  },
  {
    title: "도커, 컨테이너 빌드업!",
    qna_type: "이름/사번 변경",
    author: "박성문",
    date: "2025-02-10"
  }
];
</script>

<template>
  <QnaList :items="qnaItems" />
</template>
-->