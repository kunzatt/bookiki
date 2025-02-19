<!-- UserList.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { UserInformationForAdminResponse } from '@/types/api/user';
import type { Pageable } from '@/types/common/pagination';
import { getUserDetails, deleteUser } from '@/api/user';
import UserListItem from './UserListItem.vue';
import Pagination from '@/components/ui/Pagination/BasicWebPagination.vue';

// 상태 관리
const users = ref<UserInformationForAdminResponse[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// 페이지네이션 상태
const totalPages = ref(1);
const totalElements = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const sortBy = ref('id');
const direction = ref('desc');

// 페이지 변경 핸들러
const handlePaginationChange = async (pageInfo: Pageable) => {
  currentPage.value = pageInfo.pageNumber + 1;
  loading.value = true;

  try {
    const response = await getUserDetails(pageInfo.pageNumber, pageSize.value, 'id', 'DESC');
    users.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (err) {
    error.value = '사용자 목록을 불러오는데 실패했습니다.';
    console.error('Error fetching users:', err);
  } finally {
    loading.value = false;
  }
};

// 사용자 삭제 핸들러
const handleUserDelete = async (userId: number) => {
  try {
    await deleteUser(userId);
    await fetchUsers(); // 목록 새로고침
  } catch (err) {
    console.error('Error deleting user:', err);
    // 에러 처리 로직 추가 필요
  }
};

// 사용자 상세 정보 핸들러
const handleUserDetail = (userId: number) => {
  // 상세 페이지로 이동 또는 모달 표시 로직
  console.log('User detail:', userId);
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchUsers();
});

// fetchUsers 함수 수정
const fetchUsers = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await getUserDetails(
      currentPage.value - 1, // 백엔드는 0-based index 사용
      pageSize.value,
      sortBy.value,
      direction.value,
    );
    users.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (err) {
    error.value = '사용자 목록을 불러오는데 실패했습니다.';
    console.error('Error fetching users:', err);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="w-full">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center items-center py-8">
      <span class="text-gray-500">로딩 중...</span>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="flex justify-center items-center py-8 text-red-500">
      {{ error }}
    </div>

    <!-- 데이터 테이블 -->
    <table v-else class="w-full border-collapse">
      <thead class="bg-gray-50">
        <tr>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">회원 ID</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">이름</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">이메일</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">사번</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">도서 대출 현황</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">상태</th>
          <th class="py-4 px-4 text-left text-sm font-medium text-gray-500">회원 상태 변경</th>
        </tr>
      </thead>
      <tbody>
        <UserListItem
          v-for="user in users"
          :key="user.id"
          :user="user"
          @detail="handleUserDetail"
          @delete="handleUserDelete"
          @update="fetchUsers"
        />
      </tbody>
    </table>

    <!-- 페이지네이션 -->
    <div class="mt-4">
      <Pagination
        :current-page="currentPage"
        :total-pages="totalPages"
        :page-size="pageSize"
        @update:pageInfo="handlePaginationChange"
      />
    </div>
  </div>
</template>
