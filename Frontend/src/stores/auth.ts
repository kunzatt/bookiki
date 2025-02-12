import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AuthUser, LoginRequest, LoginResponse } from '@/types/api/user';
import { login as loginApi, logout as logoutApi } from '@/api/user';
import { useRouter } from 'vue-router';


export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse  | null>(null);

  // 로그인 처리
  const login = async (loginRequest: LoginRequest) => {
    const router = useRouter();
    try {
      const response: LoginResponse = await loginApi(loginRequest);
      
      // User 정보 설정
      user.value = response; 
      
      //await router.push('/main');
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  // 로그아웃 처리
  const logout = async () => {
    const router = useRouter();
    try {
      await logoutApi();
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
  };
});