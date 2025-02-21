<!-- AdminBookView.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseModal from '@/components/ui/Modal/BaseModal.vue';
import BookList from '@/components/ui/Admin/BookList.vue';
import BasicInput from '@/components/ui/Input/BasicInput.vue';
import BasicButton from '@/components/ui/Button/BasicButton.vue';
import { fetchAdminBookList } from '@/api/bookItem';

const router = useRouter();
const showDesktopModal = ref(false);
const searchKeyword = ref('');
const bookListRef = ref();
const books = ref([]); // 도서 목록을 저장할 ref
const currentPage = ref(0); // 현재 페이지 번호
const pageSize = ref(10); // 페이지 크기

// 메뉴 아이템 클릭 핸들러
const handleMenuClick = async (path: string) => {
  router.push(path);
};

// 검색 핸들러
const handleSearch = async () => {
  if (bookListRef.value) {
    bookListRef.value.handleSearch(searchKeyword.value);
  }
};

// 엔터키 핸들러
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearch();
  }
};

// 도서 목록 조회 함수
const fetchBooks = async () => {
  try {
    const response = await fetchAdminBookList(
      currentPage.value,
      pageSize.value,
      searchKeyword.value,
    );
    books.value = response.content; // 도서 목록 업데이트
  } catch (error) {
    console.error('도서 목록 조회 실패:', error);
  }
};

// 페이지 변경 핸들러
const handlePageChange = async (page: number) => {
  currentPage.value = page;
  await fetchBooks();
};

// 모바일 체크 함수
const isMobileDevice = () => {
  return window.innerWidth < 1024;
};

// 컴포넌트 마운트 시 모바일 체크
onMounted(() => {
  if (isMobileDevice()) {
    showDesktopModal.value = true;
  }

  // 리사이즈 이벤트 리스너
  window.addEventListener('resize', () => {
    if (isMobileDevice()) {
      showDesktopModal.value = true;
    }
  });
});

// 페이지 이동 버튼
const goToOtherPage = () => {
  router.push('/admin/addBook');
};

// 모달 닫기 핸들러
const handleModalClose = () => {
  router.push('/');
};
</script>

<template>
  <div class="h-full">
    <div class="max-w-7xl mx-auto">
      <!-- 메인 컨텐츠 영역 -->
      <div class="max-w-7xl mx-auto">
        <div class="flex justify-between items-center mb-6">
          <BasicButton text="도서 등록" @click="goToOtherPage" />
          <!-- 검색바 -->
          <div class="w-96">
            <BasicInput
              type="withButton"
              v-model="searchKeyword"
              placeholder="도서명, ISBN, 출판사, 저자로 검색"
              buttonText="검색"
              @button-click="handleSearch"
              @keyup="handleKeyPress"
            />
          </div>
        </div>

        <!-- BookList 컴포넌트 -->
        <BookList 
          ref="bookListRef"
          :keyword="searchKeyword" 
        />
      </div>
    </div>

    <!-- 데스크탑 전용 알림 모달 -->
    <BaseModal
      v-model="showDesktopModal"
      title="데스크탑 환경에서 확인해주세요"
      content="이 페이지는 데스크탑 환경에서만 이용 가능합니다. PC에서 다시 접속해 주시기 바랍니다."
      icon="desktop_windows"
      confirm-text="메인으로"
      @update:model-value="handleModalClose"
    />
  </div>
</template>
