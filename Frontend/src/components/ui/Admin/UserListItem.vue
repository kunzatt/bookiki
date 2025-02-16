<!-- UserListItem.vue -->
<script setup lang="ts">
import type { UserInformationForAdminResponse } from '@/types/api/user';
import Button from '@/components/ui/Button/BasicButton.vue';
import Badge from '@/components/ui/Badge/BasicStatusBadge.vue';

interface Props {
  user: UserInformationForAdminResponse;
}

// Props 정의
const props = defineProps<Props>();

// Emit 이벤트 정의
const emit = defineEmits<{
  (e: 'detail', userId: number): void;
  (e: 'delete', userId: number): void;
}>();

// 버튼 클릭 핸들러
const handleDetailClick = () => {
  emit('detail', props.user.id);
};

const handleDeleteClick = () => {
  emit('delete', props.user.id);
};
</script>

<template>
  <tr class="border-b border-gray-200 hover:bg-gray-50">
    <!-- 회원 ID -->
    <td class="py-4 px-4 text-sm">
      {{ user.id }}
    </td>

    <!-- 이름 -->
    <td class="py-4 px-4 text-sm">
      {{ user.userName }}
    </td>

    <!-- 이메일 -->
    <td class="py-4 px-4 text-sm">
      {{ user.email }}
    </td>

    <!-- 사번 -->
    <td class="py-4 px-4 text-sm">
      {{ user.companyId }}
    </td>

    <!-- 도서 대출 현황 -->
    <td class="py-4 px-4 text-sm">{{ user.currentBorrowCount }}권</td>

    <!-- 상태 뱃지 -->
    <td class="py-4 px-4">
      <Badge
        :text="user.hasOverdueBooks ? '연체' : '정상'"
        :type="user.hasOverdueBooks ? 'error' : 'primary'"
        :is-enabled="true"
      />
    </td>

    <!-- 액션 버튼 -->
    <td class="py-4 px-4 space-x-2">
      <Button size="S" text="변경" :is-enabled="false" @click="handleDeleteClick" />
    </td>
  </tr>
</template>
