import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth';
import HomeView from '../views/HomeView.vue';
import MainView from '@/views/MainView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('@/views/MainView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/oauth2/signup',
      name: 'OAuth2SignUp',
      component: () => import('@/views/auth/OAuth2SignUp.vue'),
      props: (route) => ({ 
        token: route.query.token 
      })
    },
    {
      path: '/api/oauth2/error',
      name: 'OAuth2Error',
      component: () => import('@/views/auth/OAuth2Error.vue')
    },
    {
      path: '/oauth2/callback',
      name: 'OAuth2Callback',
      component: () => import('@/views/auth/OAuth2Callback.vue')
    }
  ]
});

// 네비게이션 가드
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.meta.requiresAuth;

  if (requiresAuth && !authStore.isAuthenticated) {
    // 인증이 필요한 페이지에 접근 시 로그인 상태가 아니면 로그인 페이지로 리다이렉트
    next('/login');
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    // 이미 로그인된 상태에서 로그인 페이지 접근 시 메인 페이지로 리다이렉트
    next('/main');
  } else {
    next();
  }
});

export default router
