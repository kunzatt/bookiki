export interface NavItem {
    name: string;
    path: string;
    icon: string;
}

export const navItems: NavItem[] = [
    {
        name: '홈',
        path: '/',
        icon: 'home'
    },
    {
        name: '검색',
        path: '/search',
        icon: 'search'
    },
    {
        name: '도서관',
        path: '/library',
        icon: 'local_library'
    },
    {
        name: '마이',
        path: '/mypage',
        icon: 'person'
    },
    {
        name: '설정',
        path: '/settings',
        icon: 'settings'
    }
];