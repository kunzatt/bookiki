// types/sidebar.ts
export interface MenuItem {
  name: string;
  path?: string;
  icon: string;
  roles: ('USER' | 'ADMIN')[];
  action?: 'logout';
}
export interface User {
  role: 'USER' | 'ADMIN';
  userName: string;
  email: string;
}

export const menuItems: MenuItem[] = [
  {
    name: '홈',
    path: '/',
    icon: 'home',
    roles: ['USER', 'ADMIN'],
  },
  {
    name: '관리자 페이지',
    path: '/admin',
    icon: 'admin_panel_settings',
    roles: ['ADMIN'],
  },
  {
    name: '검색',
    path: '/search',
    icon: 'search',
    roles: ['USER', 'ADMIN'],
  },
  {
    name: '도서관',
    path: '/library',
    icon: 'library_books',
    roles: ['USER', 'ADMIN'],
  },
  {
    name: '마이 페이지',
    path: '/mypage',
    icon: 'person',
    roles: ['USER', 'ADMIN'],
  },
  {
    name: '로그아웃',
    icon: 'logout',
    roles: ['USER', 'ADMIN'],
    action: 'logout',
  },
];
