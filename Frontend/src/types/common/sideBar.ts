// types/sidebar.ts
export interface MenuItem {
    name: string;
    path: string;
    icon: string;
    roles: ('USER' | 'ADMIN')[];
    subItems?: SubMenuItem[];  // 관리자페이지, 마이페이지의 하위 메뉴를 위한 필드
    hasToggle?: boolean;      // 토글 여부
    isOpen?: boolean; 
}

export interface SubMenuItem {
    name: string;          // 메뉴 이름
    path?: string;          // 라우트 경로
    icon: string;         // 아이콘 (선택적)
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
        roles: ['USER', 'ADMIN']
    },
    {
        name: '관리자 페이지',
        path: '/admin',
        icon: 'admin_panel_settings',
        roles: ['ADMIN'],
        hasToggle: true,
        subItems: [
            {
                name: '관리자 페이지',
                path: '/', 
                icon: ''
            },
            {
                name: '회원 관리',
                path: '/', 
                icon: ''
            },
            {
                name: '도서 관리',
                path: '/', 
                icon: ''
            },
            {
                name: '도서관 관리',
                path: '/', 
                icon: ''
            },
            {
                name: '문의사항',
                path: '/', 
                icon: ''
            },
            {
                name: '공지사항',
                path: '/', 
                icon: ''
            },
            {
                name: '알림',
                path: '/', 
                icon: ''
            },
        ]
    },
    {
        name: '검색',
        path: '/search',
        icon: 'search',
        roles: ['USER', 'ADMIN']
    },
    {
        name: '도서관',
        path: '/library',
        icon: 'library_books',
        roles: ['USER', 'ADMIN']
    },
    {
        name: '마이 페이지',
        path: '/mypage',
        icon: 'person',
        roles: ['USER', 'ADMIN'],
        hasToggle: true,
        subItems: [
            {
                name: '마이 페이지',
                path: '/', 
                icon: ''
            },
            {
                name: '대출 중 도서',
                path: '/', 
                icon: ''
            },
            {
                name: '즐겨찾기 도서',
                path: '/', 
                icon: ''
            },
            {
                name: '나의 대출 이력',
                path: '/', 
                icon: ''
            },
            {
                name: '문의사항',
                path: '/', 
                icon: ''
            },
            {
                name: '비밀번호 변경',
                path: '/', 
                icon: ''
            },
            {
                name: '로그아웃',
                icon: '',
                action: 'logout'                
            },
        ]
    },
    {
        name: '설정',
        path: '/settings',
        icon: 'settings',
        roles: ['USER', 'ADMIN']
    }
];