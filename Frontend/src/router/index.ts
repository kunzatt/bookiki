import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import HomeView from '../views/HomeView.vue';
import MainView from '@/views/MainView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('@/views/MainView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/signup',
      name: 'SignUp',
      component: () => import('@/views/auth/SignupView.vue'),
    },
    {
      path: '/oauth2/signup',
      name: 'OAuth2SignUp',
      component: () => import('@/views/auth/OAuth2SignUp.vue'),
      props: (route) => ({
        token: route.query.token,
      }),
    },
    {
      path: '/api/oauth2/error',
      name: 'OAuth2Error',
      component: () => import('@/views/auth/OAuth2Error.vue'),
    },
    {
      path: '/oauth2/callback',
      name: 'OAuth2Callback',
      component: () => import('@/views/auth/OAuth2Callback.vue'),
    },
    {
      path: '/books/:id',
      name: 'book-detail',
      component: () => import('@/views/BookDetailView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/public/books/:id',
      name: 'PublicBookDetail',
      component: () => import('@/views/QRBookDetailView.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    // 404 페이지
    // {
    //   path: '/:pathMatch(.*)*',
    //   name: 'not-found',
    //   component: () => import('@/views/NotFoundView.vue')
    // }
    // {
    //   path: '/',
    //   name: 'home',
    //   component: HomeView,
    // },
    {
      path: '/library',
      name: 'VirtualShelf',
      component: () => import('@/views/book/VirtualShelfView.vue'),
    },
    {
      path: '/notices/create',
      name: 'NoticeCreate',
      component: () => import('@/views/notice/NoticeCreateView.vue'),
      // meta: { requiresAuth: true }, // 인증 필요 추가
    },
    {
      path: '/notices',
      name: 'NoticeList',
      component: () => import('@/views/notice/NoticeListView.vue'),
      // meta: { requiresAuth: true }, // 인증 필요 추가
    },
    {
      path: '/notices/:id',
      name: 'NoticeDetail',
      component: () => import('@/views/notice/NoticeDetailView.vue'),
      // meta: { requiresAuth: true },
    },
    {
      path: '/notices/:id/edit',
      name: 'NoticeEdit',
      component: () => import('@/views/notice/NoticeCreateView.vue'),
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('@/views/notification/NotificationView.vue'),
      meta: { requiresAuth: true }, // 인증이 필요한 페이지로 설정
    },
    {
      path: '/qnas',
      name: 'QnaList',
      component: () => import('@/views/qna/QnaListView.vue'),
      // meta: { requiresAuth: true },
    },
    {
      path: '/qnas/create',
      name: 'QnaCreate',
      component: () => import('@/views/qna/QnaCreateView.vue'),
      // meta: { requiresAuth: true },
    },
    {
      path: '/qnas/:id',
      name: 'QnaDetail',
      component: () => import('@/views/qna/QnaDetailView.vue'),
      // meta: { requiresAuth: true },
    },
    {
      path: '/qnas/:id/edit',
      name: 'QnaEdit',
      component: () => import('@/views/qna/QnaCreateView.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('@/views/mypage/MyPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/loans',
      name: 'loans',
      component: () => import('@/views/mypage/CurrentBorrowedBookList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/mypage/MyFavoriteList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'adminMain',
      component: () => import('@/views/admin/AdminView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/user',
      name: 'adminUser',
      component: () => import('@/views/admin/AdminUserView.vue'),
      meta: { requiresAuth: true },
    },
  ],
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

export default router;
