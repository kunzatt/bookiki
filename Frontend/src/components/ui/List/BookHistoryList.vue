<script setup lang="ts">
interface HistoryItem {
  title: string;      // 도서명
  category: string;   // 기록/공학 등 카테고리
  dueDate: string;    // 대출일
  returnDate: string; // 반납일
}

interface Props {
  items: HistoryItem[];
}

const props = defineProps<Props>();

// 날짜 포맷팅 함수 (YYYY.MM.DD)
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};
</script>

<template>
  <div class="w-full max-w-md mx-auto">
    <ul class="space-y-4">
      <li 
        v-for="(item, index) in items" 
        :key="index"
        class="bg-white p-4 rounded-lg shadow-sm flex justify-between"
      >
        <!-- 왼쪽: 도서 정보 -->
        <div class="flex-1">
          <h3 class="text-base font-medium text-gray-900 mb-1">
            {{ item.title }}
          </h3>
          <p class="text-sm text-gray-500">
            {{ item.category }}
          </p>
        </div>
        
        <!-- 오른쪽: 날짜 정보 -->
        <div class="ml-4 space-y-1 min-w-[140px]">
          <div class="flex justify-between text-sm">
            <span class="text-gray-500">대출일:</span>
            <span class="text-gray-900">{{ formatDate(item.dueDate) }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-500">반납일:</span>
            <span :class="item.returnDate ? 'text-[#FF6B6B]' : 'text-gray-400'">
              {{ item.returnDate ? formatDate(item.returnDate) : '-' }}
            </span>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<!-- 사용 예시
<script setup>
const historyItems = [
  {
    title: "초보자를 위한 디버깅",
    category: "기술/공학",
    dueDate: "2025-01-03",
    returnDate: ""
  },
  {
    title: "이런 책도 있고",
    category: "기술/공학",
    dueDate: "2025-01-03",
    returnDate: "2025-01-11"
  },
  {
    title: "저런 책도 있는거죠",
    category: "기술/공학",
    dueDate: "2025-01-03",
    returnDate: "2025-01-11"
  }
];
</script>

<template>
  <BookHistoryList :items="historyItems" />
</template>
-->