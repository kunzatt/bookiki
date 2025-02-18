<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import BottomNav from '@/components/common/BottomNav.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import {
  getCurrentUserInformation,
  getProfileImageData,
  logout,
  updateProfileImage,
  deleteProfileImage,
} from '@/api/user';
import { getCurrentBorrowedBooks } from '@/api/bookHistory';
import { getCurrentPolicy } from '@/api/loanPolicy';
import type { UserInformationResponse } from '@/types/api/user';

const router = useRouter();
const isLoading = ref(true);

const userInfo = ref({
  name: '',
  companyId: '',
  profileImage: '/default-book-cover.svg',
  availableLoans: 0,
});

const menuItems = ref([
  { id: 1, title: '대출 중 도서', path: '/mypage/current-borrowed' },
  { id: 2, title: '좋아요 목록', path: '/favorites' },
  { id: 3, title: '나의 대출 이력', path: '/mypage/history' },
  { id: 4, title: '문의사항', path: '/qnas' },
]);

// 사용자 데이터 가져오기
const fetchUserData = async () => {
  try {
    isLoading.value = true;

    // 사용자 정보 가져오기
    const userData: UserInformationResponse = await getCurrentUserInformation();
    console.log('받아온 사용자 데이터:', userData);

    // 프로필 이미지 가져오기
    try {
      const imageBlob = await getProfileImageData();
      const imageUrl = URL.createObjectURL(imageBlob);
      userInfo.value.profileImage = imageUrl;
    } catch (error) {
      console.error('프로필 이미지 로드 실패:', error);
      userInfo.value.profileImage = '/default-book-cover.svg';
    }

    // provider가 BOOKIKI일 때만 비밀번호 변경 메뉴 추가
    if (userData.provider === 'BOOKIKI') {
      menuItems.value.push({ id: 5, title: '비밀번호 변경', path: '/password/change' });
    }

    // 로그아웃은 항상 마지막에 추가
    menuItems.value.push({ id: 6, title: '로그아웃', path: '/logout' });

    // 현재 대출 중인 도서 목록 조회
    const currentBooks = await getCurrentBorrowedBooks();
    const currentBorrowedCount = currentBooks.length;

    // 최대 대출 가능 도서 수 조회
    const policyData = await getCurrentPolicy();
    const maxBorrowable = policyData.maxBooks;

    // 대출 가능한 도서 수 계산 (0 미만일 경우 0으로 설정)
    const availableLoans = Math.max(0, maxBorrowable - currentBorrowedCount);

    userInfo.value = {
      ...userInfo.value,
      name: userData.userName,
      companyId: userData.companyId,
      availableLoans,
    };
  } catch (error) {
    console.error('사용자 정보 로드 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 메뉴 아이템 클릭 핸들러
const handleMenuClick = async (path: string) => {
  if (path === '/logout') {
    try {
      await logout();  // 로그아웃 처리 (이미 store에서 세션을 먼저 지움)
      router.push('/login');  // 이제 router.push를 사용해도 됨
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  } else {
    router.push(path);
  }
};

// 이미지 업로드 처리
const handleImageClick = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';

  input.onchange = async (event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
      try {
        const file = target.files[0];
        await updateProfileImage(file);

        // 이미지 업로드 후 프로필 다시 불러오기
        const imageBlob = await getProfileImageData();
        const imageUrl = URL.createObjectURL(imageBlob);
        userInfo.value.profileImage = imageUrl;
      } catch (error) {
        console.error('프로필 이미지 업로드 실패:', error);
      }
    }
  };

  input.click();
};

// 이미지 로드 실패시 기본 이미지로 대체
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/default-book-cover.svg';
};

// 프로필 사진 삭제 처리
const handleImageDelete = async (event: Event) => {
  event.stopPropagation();
  try {
    await deleteProfileImage();
    await fetchUserData(); // 사용자 데이터를 다시 불러와서 기본 이미지로 업데이트
  } catch (error) {
    console.error('프로필 이미지 삭제 실패:', error);
  }
};

onMounted(() => {
  fetchUserData();
});
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <!-- 데스크톱 사이드바 - lg 이상에서만 표시 -->
    <Sidebar class="hidden lg:block" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 반응형 헤더 -->
      <HeaderMobile class="lg:hidden" />
      <HeaderDesktop class="hidden lg:block" />

      <main class="flex-1 px-5 lg:px-8 pb-16 lg:pb-8 overflow-y-auto">
        <div class="max-w-[1440px] mx-auto">
          <div v-if="isLoading" class="flex justify-center items-center h-[300px]">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
          </div>

          <template v-else>
            <!-- Profile Section -->
            <div class="flex items-center space-x-4 mb-8 pt-6">
              <div class="relative">
                <div class="profile-image-container" @click="handleImageClick">
                  <img
                    :src="userInfo.profileImage"
                    :alt="userInfo.name"
                    class="w-24 h-24 rounded-full object-cover bg-gray-100 profile-image"
                    @error="handleImageError"
                  />
                  <div class="delete-overlay" @click.stop="handleImageDelete" v-if="userInfo.profileImage !== '/default-book-cover.svg'">
                    <i class="fas fa-trash delete-icon"></i>
                  </div>
                </div>
                <button
                  @click="handleImageClick"
                  class="absolute bottom-0 right-0 bg-blue-500 p-2 rounded-full text-white hover:bg-blue-600 transition-colors"
                >
                  <span class="material-icons text-lg">photo_camera</span>
                </button>
              </div>
              <div>
                <h2 class="text-xl font-semibold">{{ userInfo.name }}</h2>
                <p class="text-gray-600">{{ userInfo.companyId }}</p>
                <span
                  v-if="userInfo.availableLoans > 0"
                  class="inline-block mt-1 px-2 py-1 bg-green-100 text-green-800 rounded text-sm"
                >
                  현재 {{ userInfo.availableLoans }}권 대출 가능
                </span>
              </div>
            </div>

            <!-- Menu Items -->
            <div class="space-y-2">
              <button
                v-for="item in menuItems"
                :key="item.id"
                class="w-full text-left px-4 py-3 flex items-center justify-between bg-white hover:bg-gray-50 rounded-lg transition-colors border border-gray-100"
                @click="handleMenuClick(item.path)"
              >
                <span>{{ item.title }}</span>
                <span class="text-gray-400">›</span>
              </button>
            </div>
          </template>
        </div>
      </main>

      <!-- 모바일에서만 BottomNav 표시 -->
      <div class="lg:hidden">
        <BottomNav />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Material Icons 폰트 스타일 */
.material-icons {
  font-family: 'Material Icons';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

/* 버튼 호버 효과 */
button {
  transition: all 0.2s ease;
}

button:hover {
  transform: translateY(-1px);
}

/* 반응형 스타일링 */
@media screen and (min-width: 768px) {
  .grid {
    @apply grid-cols-3;
  }
}

@media screen and (min-width: 1024px) {
  .grid {
    @apply grid-cols-4;
  }
}

.container {
  scroll-behavior: smooth;
}

.flex {
  will-change: transform;
}

/* 프로필 이미지 스타일 */
.profile-image {
  @apply rounded-full object-cover;
  aspect-ratio: 1 / 1;
}

.profile-image-container {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.profile-image-container:hover .delete-overlay {
  opacity: 1;
}

.delete-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.delete-icon {
  color: white;
  font-size: 24px;
}

/* 메뉴 아이템 스타일 */
.menu-item {
  @apply w-full text-left px-4 py-3 flex items-center justify-between 
         bg-white hover:bg-gray-50 rounded-lg transition-colors 
         border border-gray-100;
}

/* 로딩 스피너 스타일 */
.loading-spinner {
  @apply animate-spin rounded-full border-b-2 border-gray-900;
}
</style>
