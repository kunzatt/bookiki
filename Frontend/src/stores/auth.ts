import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { LoginRequest, LoginResponse } from '@/types/api/user';
import { login as loginApi, logout as logoutApi } from '@/api/user';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse | null>(null);
  const router = useRouter();

  // 초기화 함수 추가
  const initialize = () => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      user.value = JSON.parse(savedUser);
    }
  };

  // 로그인 처리
  const login = async (loginRequest: LoginRequest) => {
    try {
      const response: LoginResponse = await loginApi(loginRequest);
      
      // User 정보 설정
      user.value = response;
      
      // 로그인 성공 시 localStorage에 저장
      localStorage.setItem('user', JSON.stringify(response));
      
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
      
      // localStorage에서 제거
      localStorage.removeItem('user');
      
      user.value = null;
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
    initialize
  };
});