import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AuthUser, LoginRequest, LoginResponse } from '@/types/api/user';
import { login as loginApi, logout as logoutApi, setToken as setTokenApi } from '@/api/user';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse | null>(null);
  const error = ref<string | null>(null);
  const loading = ref(false);
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
    loading.value = true;
    error.value = null;

    try {
      // 먼저 상태 초기화
      user.value = null;
      sessionStorage.clear(); // 모든 세션 데이터 삭제

      // 그 다음 API 호출
      await logoutApi();
      return true;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '로그아웃에 실패했습니다.';
      error.value = errorMessage;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  // 권한 체크 헬퍼 함수
  const hasRole = (requiredRole: string) => {
    return userRole.value === requiredRole;
  };

  const setToken = async (token: string) => {
    try {
      console.log('토큰 설정 시작:', token.substring(0, 10) + '...');

      // 토큰을 쿠키에 설정
      document.cookie = `access_token=${token}; path=/; secure; max-age=1800`;

      const response = await setTokenApi(token);
      console.log('서버 응답:', response);

      // 응답 데이터 검증
      if (!response || !response.id || !response.username || !response.role) {
        throw new Error('유효하지 않은 사용자 정보');
      }

      const userData = {
        id: response.id,
        username: response.username,
        role: response.role,
      };

      user.value = userData;
      sessionStorage.setItem('user', JSON.stringify(userData));

      console.log('토큰 설정 완료:', userData);
      return response;
    } catch (error) {
      console.error('토큰 설정 실패:', error);
      user.value = null;
      sessionStorage.removeItem('user');
      throw error;
    }
  };

  const handleOAuth2Login = async (token: string) => {
    try {
      loading.value = true;
      const response = await setTokenApi(token);

      user.value = {
        id: response.id,
        username: response.username,
        role: response.role,
      };

      sessionStorage.setItem('user', JSON.stringify(user.value));

      // 토큰 설정과 유저 정보 저장이 완료될 때까지 기다림
      await new Promise((resolve) => setTimeout(resolve, 100));

      // 라우터로 메인 페이지로 이동
      router.push('/main');

      return true;
    } catch (error) {
      console.error('OAuth2 로그인 실패:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  // const handleOAuth2Login = async (token: string) => {
  //   try {
  //     const response = await setTokenApi(token);

  //     // User 정보 설정 (response에서 받은 데이터로)
  //     user.value = {
  //       id: response.id,
  //       username: response.username,
  //       role: response.role,
  //     };

  //     // sessionStorage에 사용자 정보 저장
  //     sessionStorage.setItem('user', JSON.stringify(user.value));
  //     return true;
  //   } catch (error) {
  //     console.error('OAuth2 login failed:', error);
  //     throw error;
  //   }
  // };

  // Getters
  const isAuthenticated = computed(() => !!user.value);
  const userRole = computed(() => user.value?.role);
  const userName = computed(() => user.value?.username);

  return {
    user,
    error,
    loading,
    isAuthenticated,
    userRole,
    userName,
    login,
    logout,
    initializeFromStorage,
    hasRole,
    setToken,
    handleOAuth2Login,
  };
});
