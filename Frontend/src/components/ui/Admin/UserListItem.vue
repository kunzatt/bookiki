<!-- UserListItem.vue -->
<script setup lang="ts">
import type { UserInformationForAdminResponse } from '@/types/api/user';
import Button from '@/components/ui/Button/BasicButton.vue';
import Badge from '@/components/ui/Badge/BasicStatusBadge.vue';
import SingleDatePicker from '@/components/ui/DatePicker/SingleDatePicker.vue';
import { ref, computed } from 'vue';
import { updateUserActiveAt } from '@/api/user';
import dayjs from 'dayjs';

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

// 사용자 상태 계산
const userStatus = computed(() => {
  console.log('User data:', props.user);
  console.log('Active At raw value:', props.user.activeAt);
  
  const today = dayjs().startOf('day');
  
  // activeAt이 배열로 오는 경우 처리
  let activeAt = null;
  if (props.user.activeAt) {
    if (Array.isArray(props.user.activeAt)) {
      const [year, month, day, hour, minute] = props.user.activeAt;
      activeAt = dayjs(`${year}-${month}-${day} ${hour}:${minute}`).startOf('day');
    } else {
      activeAt = dayjs(props.user.activeAt).startOf('day');
    }
  }
  
  console.log('Today:', today.format('YYYY-MM-DD'));
  console.log('ActiveAt:', activeAt?.format('YYYY-MM-DD') || 'No active date');
  console.log('Is After:', activeAt?.isAfter(today) || false);
  console.log('Has Overdue:', props.user.hasOverdueBooks);
  
  if (activeAt && activeAt.isAfter(today)) {
    return {
      text: '대출 불가',
      type: 'error',
      isEnabled: true
    };
  }
  
  if (props.user.hasOverdueBooks) {
    return {
      text: '연체',
      type: 'error',
      isEnabled: true
    };
  }
  
  return {
    text: '정상',
    type: 'primary',
    isEnabled: true
  };
});

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
  <tr class="hover:bg-gray-50">
    <!-- 회원 ID -->
    <td class="whitespace-nowrap py-4 pl-4 pr-3 text-sm text-gray-900 text-center sm:pl-6">
      {{ user.id }}
    </td>

    <!-- 이름 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-900 text-center">
      {{ user.userName }}
    </td>

    <!-- 이메일 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-900 text-center">
      {{ user.email }}
    </td>

    <!-- 사번 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-900 text-center">
      {{ user.companyId }}
    </td>

    <!-- 도서 대출 현황 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-900 text-center">
      {{ user.currentBorrowCount }}권
    </td>

    <!-- 상태 뱃지 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-center">
      <div class="flex justify-center">
        <Badge
          :text="userStatus.text"
          :type="userStatus.type"
          :is-enabled="userStatus.isEnabled"
        />
      </div>
    </td>

    <!-- 액션 버튼 -->
    <td class="whitespace-nowrap px-3 py-4 text-sm text-center">
      <div class="flex justify-center items-center space-x-2">
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
      </div>
    </td>
  </tr>
</template>
