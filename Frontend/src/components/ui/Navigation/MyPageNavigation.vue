<script setup lang="ts">
import NavigationMenuItem from './NavigationMenuItem.vue';
import BasicButton from '../Button/BasicButton.vue';
import LogoutModal from '@/components/modal/LogoutModal.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

// 마이페이지 dto 생성 후 수정 필요

// interface MyPageProps {
//   /**
//    * 사용자 정보
//    */
//   interface User {
//     id: number;        
//     email: string;
//     provider: 'BOOKIKI' | 'NAVER' | 'GOOGLE';
//     password?: string;
//     user_name: string;
//     company_id: string;
//     role: 'USER' | 'ADMIN';
//     created_at: string;
//     updated_at: string;
//     active_at?: string;
//     profile_image?: string;
//     deleted: number;
//     status: {
//       availableLoans: number;
//       isOverdue: boolean;
//       isSuspended: boolean;
//     };
//   }
// }

const props = defineProps();

const router = useRouter();
const authStore = useAuthStore();
const showLogoutModal = ref(false);

// router 변경 필요
const menuItems = [
  { title: '대출 중 도서', to: '/mypage/current-loans' },
  { title: '즐겨찾기 도서', to: '/mypage/favorites' },
  { title: '나의 대출 이력', to: '/mypage/loan-history' },
  { title: '문의사항', to: '/mypage/qna' },
  { title: '비밀번호 변경', to: '/mypage/change-password' },
  { title: '로그아웃', to: '/logout', isHighlighted: true }
];

const handleProfileChange = () => {
  // 프로필 사진 변경 로직
  console.log('프로필 사진 변경');
};

const handleClick = (to: string) => {
  if (to === '/logout') {
    showLogoutModal.value = true;
  } else {
    router.push(to);
  }
};

const handleLogoutConfirm = async () => {
  try {
    await authStore.logout();
    router.push('/login'); // 로그아웃 후 로그인 페이지로 리다이렉트
  } catch (error) {
    console.error('로그아웃 실패:', error);
  }
};
</script>

<template>
  <div class="w-full max-w-md mx-auto">
    <!-- 프로필 섹션 -->
    <div class="flex items-start space-x-4 mb-6">
      <div class="w-16 h-16 rounded-full overflow-hidden">
        <img 
          :src="props.user.profileImage || '/default-profile.jpg'" 
          :alt="props.user.user_name"
          class="w-full h-full object-cover"
        />
      </div>
      <div class="flex-1">
        <h2 class="text-xl font-medium text-gray-900">{{ props.user.user_name }}</h2>
        <p class="text-sm text-gray-500">{{ props.user.id }}</p>
        <div class="mt-2">
          <BasicButton 
            size="M" 
            text="프로필사진 변경" 
            :isEnabled="false"
            @click="handleProfileChange"
          />
        </div>
      </div>
    </div>

    <!-- 상태 정보 -->
    <div class="bg-gray-50 p-4 rounded-lg mb-6">
      <p class="text-sm text-gray-600">
        현재 2권 대출 가능 / 연체 / 일시정지
      </p>
    </div>

    <!-- 네비게이션 메뉴 -->
    <nav class="border-t border-gray-200">
      <NavigationMenuItem 
        v-for="item in menuItems"
        :key="item.title"
        :title="item.title"
        :to="item.to"
        :isHighlighted="item.isHighlighted"
        @click="handleClick(item.to)"
        class="border-b border-gray-200"
      />
    </nav>
    <LogoutModal
      :is-open="showLogoutModal"
      @confirm="handleLogoutConfirm"
      @cancel="showLogoutModal = false"
    />
  </div>
</template>