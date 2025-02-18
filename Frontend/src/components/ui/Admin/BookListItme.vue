<script setup lang="ts">
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import BasicStatusBadge from '@/components/ui/Badge/BasicStatusBadge.vue';
import type { BookAdminListResponse } from '@/types/api/bookItem';
import type { BadgeStatus } from '@/types/common/ui';
import { BookStatus } from '@/types/enums/bookStatus';

interface Props {
  book: BookAdminListResponse;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  updateStatus: [bookId: number, newStatus: BookStatus];
}>();

// 도서 상태에 따른 Badge 설정
const getStatusBadgeProps = (status: string): { text: string; type: BadgeStatus } => {
  switch (status) {
    case 'AVAILABLE':
      return { text: '대출 가능', type: 'primary' };
    case 'BORROWED':
      return { text: '대출중', type: 'warning' };
    case 'UNAVAILABLE':
      return { text: '분실', type: 'error' };
    default:
      return { text: status, type: 'gray' };
  }
};

// 상태 변경 버튼의 설정
const getActionButtonProps = (status: string) => {
  if (status === 'UNAVAILABLE') {
    return {
      text: '찾음',
      isEnabled: true,
      newStatus: 'AVAILABLE',
    };
  } else {
    // AVAILABLE이나 BORROWED일 때
    return {
      text: '분실',
      isEnabled: true,
      newStatus: 'UNAVAILABLE',
    };
  }
};

const handleStatusUpdate = () => {
  const { newStatus } = getActionButtonProps(props.book.bookStatus);
  emit('updateStatus', props.book.id, newStatus as BookStatus);
};
</script>

<template>
  <tr class="border-b border-gray-200 hover:bg-gray-50">
    <!-- 도서 ID -->
    <td class="py-4 px-4 text-sm">
      {{ book.id }}
    </td>

    <!-- 도서명 -->
    <td class="py-4 px-4 text-sm">
      <router-link :to="{ name: 'admin-book-detail', params: { id: book.id } }">
        <div class="line-clamp-2 max-w-[300px]">
          {{ book.title }}
        </div>
      </router-link>
    </td>

    <!-- ISBN -->
    <td class="py-4 px-4 text-sm">
      {{ book.isbn }}
    </td>

    <!-- 카테고리 -->
    <td class="py-4 px-4 text-sm">
      {{ book.category }}
    </td>

    <!-- 상태 -->
    <td class="py-4 px-4 text-sm whitespace-nowrap">
      <BasicStatusBadge
        :text="getStatusBadgeProps(book.bookStatus).text"
        :type="getStatusBadgeProps(book.bookStatus).type"
      />
    </td>

    <!-- QR 코드 -->
    <td class="py-4 px-4 text-sm">
      {{ book.qrCode ?? '-' }}
    </td>

    <td class="py-4 px-4">
      <BasicButton
        size="S"
        :text="getActionButtonProps(book.bookStatus).text"
        :is-enabled="getActionButtonProps(book.bookStatus).isEnabled"
        @click="handleStatusUpdate"
      />
    </td>
  </tr>
</template>
