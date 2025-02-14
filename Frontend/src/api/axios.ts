// src/api/axios.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

let isRefreshing = false;
let failedQueue: any[] = [];

const instance = axios.create({
  baseURL: 'http://localhost:8088', // 또는 환경변수 사용
  withCredentials: true,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

const processQueue = (error: any = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

// Request 인터셉터
instance.interceptors.request.use(
  (config) => {
    // token 관련 로직 제거 (쿠키로 관리되므로)
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// Response 인터셉터
instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();

    if (error.response?.status === 401) {
      // 리프레시 토큰 요청이 실패한 경우는 바로 로그아웃
      if (error.config.url === '/api/auth/reissue') {
        authStore.user = null;
        sessionStorage.removeItem('user');
        await authStore.logout();
        return Promise.reject(error);
      }

      // 다른 API 요청이 401인 경우 리프레시 시도
      if (!error.config._retry) {
        error.config._retry = true;

        try {
          await instance.post('/api/auth/reissue');
          return instance(error.config);
        } catch (refreshError) {
          authStore.user = null;
          sessionStorage.removeItem('user');
          await authStore.logout();
          return Promise.reject(refreshError);
        }
      }
    }

    return Promise.reject(error);
  },
);
왜;

export default instance;
