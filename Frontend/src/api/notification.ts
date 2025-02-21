import instance from '@/api/axios'; // 기존 설정된 axios instance 임포트

const API_URL = '/api/notifications';

import type { NotificationResponse } from '@/types/api/notification';
import type { PageResponse } from '@/types/common/pagination';

// 사용자 알림 목록 조회
export const getUserNotifications = async (pageable?: {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}): Promise<PageResponse<NotificationResponse>> => {
  const response = await instance.get<PageResponse<NotificationResponse>>(API_URL, {
    params: pageable,
  });
  return response.data;
};

// 읽지 않은 알림 존재 여부 확인
export const hasUnreadNotifications = async (): Promise<boolean> => {
  const response = await instance.get<boolean>(`${API_URL}/unread`);
  return response.data;
};

// 알림 상세 조회
export const getNotification = async (notificationId: number): Promise<NotificationResponse> => {
  const response = await instance.get<NotificationResponse>(`${API_URL}/${notificationId}`);
  return response.data;
};

// 알림 읽음 처리
export const updateNotificationReadStatus = async (notificationId: number): Promise<void> => {
  await instance.put(`${API_URL}/${notificationId}`);
};

// 알림 삭제
export const deleteNotification = async (notificationId: number): Promise<void> => {
  await instance.delete(`${API_URL}/${notificationId}`);
};
