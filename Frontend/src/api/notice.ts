import axios from 'axios';

const API_URL = '/api';

import type {
    NoticeRequest,
    NoticeResponse,
    NoticeUpdate
} from '@/types/api/notice';
import type { PageResponse } from '@/types/common/pagination';

// 공지사항 등록
export const createNotice = async (request: NoticeRequest): Promise<number> => {
    try {
        const response = await axios.post<number>(
            `${API_URL}/admin/notices`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('공지사항 등록 실패:', error);
        throw error;
    }
};

// 공지사항 목록 조회
export const selectAllNotices = async (
    params: {
        keyword?: string;
        pageable?: {
            page?: number;
            size?: number;
            sort?: string;
            direction?: 'asc' | 'desc';
        };
    }
): Promise<PageResponse<NoticeResponse>> => {
    try {
        const { pageable, ...restParams } = params;
        const response = await axios.get<PageResponse<NoticeResponse>>(`${API_URL}/notices`, {
            params: {
                ...restParams,
                ...pageable
            }
        });
        return response.data;
    } catch (error) {
        console.error('공지사항 목록 조회 실패:', error);
        throw error;
    }
};

// 공지사항 상세 조회
export const selectNoticeById = async (id: number): Promise<NoticeResponse> => {
    try {
        const response = await axios.get<NoticeResponse>(`${API_URL}/notices/${id}`);
        return response.data;
    } catch (error) {
        console.error('공지사항 상세 조회 실패:', error);
        throw error;
    }
};

// 공지사항 삭제
export const deleteNotice = async (id: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/admin/notices/${id}`);
    } catch (error) {
        console.error('공지사항 삭제 실패:', error);
        throw error;
    }
};

// 공지사항 수정
export const updateNotice = async (update: NoticeUpdate): Promise<string> => {
    try {
        const response = await axios.put<string>(
            `${API_URL}/admin/notices`,
            update
        );
        return response.data;
    } catch (error) {
        console.error('공지사항 수정 실패:', error);
        throw error;
    }
};