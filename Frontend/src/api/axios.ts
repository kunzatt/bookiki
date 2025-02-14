import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088',
  withCredentials: true,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();

    if (error.response?.status === 401) {
      authStore.user = null;
      sessionStorage.removeItem('user');
      await authStore.logout();

      const currentPath = router.currentRoute.value.fullPath;
      if (currentPath !== '/login') {
        router.push({
          path: '/login',
          query: { redirect: currentPath },
        });
      }
    }

    console.error('API Error:', {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
    });

    return Promise.reject(error);
  },
);

export default instance;
