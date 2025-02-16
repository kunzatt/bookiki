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
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = ref(10);
const pageInfo = ref<Pageable>({
  pageNumber: 0,
  pageSize: 10,
  sort: ['id,DESC'],
});

// 사용자 목록 조회
const fetchUsers = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await getUserDetails();
    users.value = response;
    totalPages.value = Math.ceil(response.length / pageSize.value);
  } catch (err) {
    error.value = '사용자 목록을 불러오는데 실패했습니다.';
    console.error('Error fetching users:', err);
  } finally {
    loading.value = false;
  }
};

// 페이지 변경 핸들러
const handlePageChange = (newPageInfo: Pageable) => {
  pageInfo.value = newPageInfo;
  currentPage.value = newPageInfo.pageNumber + 1;
  fetchUsers();
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
        />
      </tbody>
    </table>

    <!-- 페이지네이션 -->
    <div class="mt-4">
      <Pagination
        v-model:pageInfo="pageInfo"
        :current-page="currentPage"
        :total-pages="totalPages"
        :page-size="pageSize"
      />
    </div>
  </div>
</template>
