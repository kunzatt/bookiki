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
  <div class="h-full">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 로딩 상태 -->
      <div v-if="loading" class="flex justify-center items-center py-8">
        <span class="text-gray-500">로딩 중...</span>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" class="flex justify-center items-center py-8 text-red-500">
        {{ error }}
      </div>

      <!-- 데이터 테이블 -->
      <div v-else class="mt-8 flex flex-col">
        <div class="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div class="inline-block min-w-full py-2 align-middle">
            <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 sm:rounded-lg">
              <table class="min-w-full divide-y divide-gray-300">
                <thead class="bg-gray-50">
                  <tr>
                    <th scope="col" class="py-3.5 pl-4 pr-3 text-center text-sm font-semibold text-gray-900 sm:pl-6">회원 ID</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">이름</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">이메일</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">사번</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">도서 대출 현황</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">상태</th>
                    <th scope="col" class="px-3 py-3.5 text-center text-sm font-semibold text-gray-900">회원 상태 변경</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200 bg-white">
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
            </div>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div class="mt-6 flex justify-center">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :page-size="pageSize"
          @update:pageInfo="handlePaginationChange"
        />
      </div>
    </div>
  </div>
</template>
