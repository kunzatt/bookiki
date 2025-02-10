// types.ts
export type HeaderType = 'default' | 'main' | 'auth';

export interface HeaderProps {
  type?: HeaderType;
  title: string;
  hasNewNotification?: boolean;
}