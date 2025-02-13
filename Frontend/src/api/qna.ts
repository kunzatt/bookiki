import axios from 'axios';

const API_URL = '/api';

import type {
    QnaRequest,
    QnaListResponse,
    QnaDetailResponse,
    QnaUpdate,
    QnaCommentRequest,
    QnaCommentUpdate
} from '@/types/api/qna';
import type { PageResponse } from '@/types/common/pagination';

// QNA API
export const createQna = async (request: QnaRequest): Promise<number> => {
    try {
        const response = await axios.post<number>(
            `${API_URL}/qna`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('문의사항 등록 실패:', error);
        throw error;
    }
};

export const selectQnas = async (
    params: {
        keyword?: string;
        qnaType?: string;
        answered?: boolean;
        pageable?: {
            page?: number;
            size?: number;
            sort?: string;
            direction?: 'asc' | 'desc';
        };
    }
): Promise<PageResponse<QnaListResponse>> => {
    try {
        const { pageable, ...restParams } = params;
        const response = await axios.get<PageResponse<QnaListResponse>>(`${API_URL}/qna`, {
            params: {
                ...restParams,
                ...pageable
            }
        });
        return response.data;
    } catch (error) {
        console.error('문의사항 목록 조회 실패:', error);
        throw error;
    }
};

export const selectQnaById = async (id: number): Promise<QnaDetailResponse> => {
    try {
        const response = await axios.get<QnaDetailResponse>(`${API_URL}/qna/${id}`);
        return response.data;
    } catch (error) {
        console.error('문의사항 상세 조회 실패:', error);
        throw error;
    }
};

export const deleteQna = async (id: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/qna/${id}`);
    } catch (error) {
        console.error('문의사항 삭제 실패:', error);
        throw error;
    }
};

export const updateQna = async (update: QnaUpdate): Promise<string> => {
    try {
        const response = await axios.put<string>(`${API_URL}/qna`, update);
        return response.data;
    } catch (error) {
        console.error('문의사항 수정 실패:', error);
        throw error;
    }
};

// QNA Comment API (관리자용)
export const createQnaComment = async (request: QnaCommentRequest): Promise<number> => {
    try {
        const response = await axios.post<number>(
            `${API_URL}/admin/qna`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('문의사항 답변 등록 실패:', error);
        throw error;
    }
};

export const deleteQnaComment = async (id: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/admin/qna/${id}`);
    } catch (error) {
        console.error('문의사항 답변 삭제 실패:', error);
        throw error;
    }
};

export const updateQnaComment = async (update: QnaCommentUpdate): Promise<string> => {
    try {
        const response = await axios.put<string>(
            `${API_URL}/admin/qna`,
            update
        );
        return response.data;
    } catch (error) {
        console.error('문의사항 답변 수정 실패:', error);
        throw error;
    }
};