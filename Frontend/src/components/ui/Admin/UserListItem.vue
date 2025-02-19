<!-- UserListItem.vue -->
<script setup lang="ts">
import type { UserInformationForAdminResponse } from '@/types/api/user';
import Button from '@/components/ui/Button/BasicButton.vue';
import Badge from '@/components/ui/Badge/BasicStatusBadge.vue';
import SingleDatePicker from '@/components/ui/DatePicker/SingleDatePicker.vue';
import { ref } from 'vue';
import { updateUserActiveAt } from '@/api/user';

interface Props {
  user: UserInformationForAdminResponse;
}

// Props 정의
const props = defineProps<Props>();

// Emit 이벤트 정의
const emit = defineEmits<{
  (e: 'detail', userId: number): void;
  (e: 'delete', userId: number): void;
  (e: 'update', userId: number): void;
}>();

// 상태 관리
const selectedDate = ref<Date | null>(null);

// 버튼 클릭 핸들러
const handleDetailClick = () => {
  emit('detail', props.user.id);
};

const handleDeleteClick = () => {
  emit('delete', props.user.id);
};

const handleDateChange = async (date: Date) => {
  try {
    await updateUserActiveAt(props.user.id, {
      activeAt: date.toISOString(),
    });
    emit('update', props.user.id);
  } catch (error) {
    console.error('Failed to update user active date:', error);
  }
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
    <td class="py-4 px-4 space-x-2 flex items-center">
      <SingleDatePicker
        v-model="selectedDate"
        @change="handleDateChange"
      />
      <Button 
        size="S" 
        text="삭제" 
        type="error" 
        :is-enabled="true" 
        @click="handleDeleteClick" 
        class="bg-red-500 hover:bg-red-600 text-white !h-[38px] !w-[72px] text-sm" 
      />
    </td>
  </tr>
</template>
