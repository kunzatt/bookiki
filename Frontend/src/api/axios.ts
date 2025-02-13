// src/api/axios.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const instance = axios.create({
  baseURL: 'http://localhost:8088',  // 또는 환경변수 사용
  withCredentials: true,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// // Request 인터셉터 추가
// instance.interceptors.request.use(
//   (config) => {
//     const token = localStorage.getItem('token');
//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//     }
//     return config;
//   },
//   (error) => {
//     return Promise.reject(error);
//   }
// );

// Request 인터셉터
instance.interceptors.request.use(
  (config) => {
    // token 관련 로직 제거 (쿠키로 관리되므로)
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// // Response 인터셉터 추가
// instance.interceptors.response.use(
//   (response) => {
//     return response;
//   },
//   (error) => {
//     console.error('API Error:', {
//       status: error.response?.status,
//       data: error.response?.data,
//       config: error.config
//     });
//     return Promise.reject(error);
//   }
// );
// Response 인터셉터
instance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const authStore = useAuthStore();

    // 401 Unauthorized 에러 처리
    if (error.response?.status === 401) {
      authStore.user = null;  // 유저 정보 초기화
      sessionStorage.removeItem('user');  // 세션에서도 제거
      await authStore.logout();
      return Promise.reject(error);
    }

    console.error('API Error:', {
      status: error.response?.status,
      data: error.response?.data,
      config: error.config
    });
    
    return Promise.reject(error);
  }
);

export default instance;