import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/main',
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('@/views/MainView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false },
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
      props: (route) => ({
        token: route.query.token,
      }),
    },
    {
      path: '/oauth2/authorization/:provider',
      name: 'OAuth2Authorization',
      component: () => import('@/views/auth/OAuth2Authorization.vue'),
    },
    {
      path: '/books/:id',
      name: 'book-detail',
      component: () => import('@/views/book/BookDetailView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/qr/books/:id',
      name: 'QRBookDetail',
      component: () => import('@/views/book/QRBookDetailView.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    {
      path: '/library',
      name: 'VirtualShelf',
      component: () => import('@/views/book/VirtualShelfView.vue'),
    },
    {
      path: '/notices/create',
      name: 'NoticeCreate',
      component: () => import('@/views/notice/NoticeCreateView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/notices',
      name: 'NoticeList',
      component: () => import('@/views/notice/NoticeListView.vue'),
    },
    {
      path: '/notices/:id',
      name: 'NoticeDetail',
      component: () => import('@/views/notice/NoticeDetailView.vue'),
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
      path: '/mypage/current-borrowed',
      name: 'currentBorrowed',
      component: () => import('@/views/mypage/CurrentBorrowedBookListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage/history',
      name: 'borrowHistory',
      component: () => import('@/views/mypage/BorrowHistoryView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('@/views/mypage/MyPageView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/mypage/MyFavoriteListView.vue'),
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
    {
      path: '/admin/books',
      name: 'adminBooks',
      component: () => import('@/views/admin/AdminBooksView.vue'),
      meta: { requiresAuth: true },
    },

    {
      path: '/admin/books/:id',
      name: 'admin-book-detail',
      component: () => import('@/views/admin/AdminBookDetailView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/password/change',
      name: 'passwordChange',
      component: () => import('@/views/password/PasswordChangeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reset-password/:token',
      name: 'PasswordReset',
      component: () => import('@/views/password/PasswordResetView.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    {
      path: '/feedbacks',
      name: 'chatbotFeedback',
      component: () => import('@/views/feedback/FeedbackListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/feedbacks/:id',
      name: 'FeedbackDetail',
      component: () => import('@/views/feedback/FeedbackDetailVIew.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/search/SearchView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/library',
      name: 'LibraryManagement',
      component: () => import('@/views/admin/LibraryManagementVIew.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/addBook',
      name: 'AddBookItem',
      component: () => import('@/views/admin/AdminAddBookView.vue'),
      meta: { requiresAuth: true },
    },
  ],
});

// 네비게이션 가드
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.meta.requiresAuth;

  // OAuth2 인증 처리
  if (to.name === 'OAuth2Authorization') {
    const provider = to.params.provider as string;
    if (provider) {
      window.location.href = `${import.meta.env.VITE_API_URL}/api/oauth2/authorization/${provider}`;
      return;
    }
    next({ name: 'OAuth2Error' });
    return;
  }

  // 로그인 상태에서 로그인 페이지 접근 시 메인으로 리다이렉트
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/');
    return;
  }

  // 인증이 필요한 페이지에 대한 처리
  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;
