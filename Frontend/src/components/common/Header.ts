// types.ts
export type HeaderType = 'default' | 'main' | 'auth';

export interface MobileHeaderProps {
  type?: HeaderType;
  title: string;
  hasNewNotification?: boolean;
}

export interface DesktopHeaderProps {
  title: string;
  hasNewNotification?: boolean;
  isAuthPage?: boolean;
}