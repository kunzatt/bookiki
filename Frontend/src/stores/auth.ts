import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AuthUser, LoginRequest, LoginResponse } from '@/types/api/user';
import { login as loginApi, logout as logoutApi } from '@/api/user';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse | null>(null);
  const router = useRouter();

  // sessionStorage에서 유저 정보 복구
  const initializeFromStorage = () => {
    const storedUser = sessionStorage.getItem('user');
    if (storedUser) {
      user.value = JSON.parse(storedUser);
    }
  };

  // 로그인 처리
  const login = async (loginRequest: LoginRequest) => {
    try {
      const response: LoginResponse = await loginApi(loginRequest);
      
      // User 정보 설정
      user.value = response;
      // sessionStorage에 사용자 정보 저장
      sessionStorage.setItem('user', JSON.stringify(response));
      
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  // 로그아웃 처리
  const logout = async () => {
    try {
      await logoutApi();
      user.value = null;
      // sessionStorage에서 사용자 정보 삭제
      sessionStorage.removeItem('user');
      await router.push('/login');
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  };

  // Getters
  const isAuthenticated = computed(() => !!user.value);
  const userRole = computed(() => user.value?.role);

  return {
    user,
    isAuthenticated,
    userRole,
    login,
    logout,
    initializeFromStorage
  };
});
