import axios from 'axios';

const API_URL = '/api/notifications';

import type { NotificationResponse } from '@/types/api/notification';
import type { PageResponse } from '@/types/common/pagination';

// 사용자 알림 목록 조회
export const getUserNotifications = async (
    pageable?: {
        page?: number;
        size?: number;
        sort?: string;
        direction?: 'asc' | 'desc';
    }
): Promise<PageResponse<NotificationResponse>> => {
    try {
        const response = await axios.get<PageResponse<NotificationResponse>>(
            `${API_URL}`,
            { params: pageable }
        );
        return response.data;
    } catch (error) {
        console.error('알림 목록 조회 실패:', error);
        throw error;
    }
};

// 알림 상세 조회
export const getNotification = async (notificationId: number): Promise<NotificationResponse> => {
    try {
        const response = await axios.get<NotificationResponse>(
            `${API_URL}/${notificationId}`
        );
        return response.data;
    } catch (error) {
        console.error('알림 상세 조회 실패:', error);
        throw error;
    }
};

// 알림 읽음 처리
export const updateNotificationReadStatus = async (notificationId: number): Promise<void> => {
    try {
        await axios.put(`${API_URL}/${notificationId}`);
    } catch (error) {
        console.error('알림 읽음 처리 실패:', error);
        throw error;
    }
};

// 알림 삭제
export const deleteNotification = async (notificationId: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/${notificationId}`);
    } catch (error) {
        console.error('알림 삭제 실패:', error);
        throw error;
    }
};