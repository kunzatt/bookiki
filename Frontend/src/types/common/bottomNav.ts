export interface NavItem {
  name: string;
  path: string;
  icon: string;
}

export const navItems: NavItem[] = [
  {
    name: '홈',
    path: '/main',
    icon: 'home',
  },
  {
    name: '검색',
    path: '/search',
    icon: 'search',
  },
  {
    name: '도서관',
    path: '/library',
    icon: 'local_library',
  },
  {
    name: '마이',
    path: '/mypage',
    icon: 'person',
  },
  {
    name: '공지사항',
    path: '/notices',
    icon: 'announcement',
  },
];
