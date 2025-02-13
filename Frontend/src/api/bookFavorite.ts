import axios from 'axios';

const API_URL = '/api/favorites';

import type { BookFavoriteResponse } from '@/types/api/favorite';
import type { PageResponse } from '@/types/common/pagination';

// 도서 좋아요 여부 확인
export const checkFavorite = async (bookItemId: number): Promise<boolean> => {
    try {
        const response = await axios.get<boolean>(`${API_URL}/${bookItemId}`);
        return response.data;
    } catch (error) {
        console.error('도서 좋아요 여부 확인 실패:', error);
        throw error;
    }
};

// 내 좋아요 목록 조회
export const getUserFavorites = async (
    page: number = 0,
    size: number = 20,
    sort?: string
): Promise<PageResponse<BookFavoriteResponse>> => {
    try {
        const response = await axios.get<PageResponse<BookFavoriteResponse>>(`${API_URL}`, {
            params: { page, size, sort }
        });
        return response.data;
    } catch (error) {
        console.error('좋아요 목록 조회 실패:', error);
        throw error;
    }
};

// 도서 좋아요 토글
export const toggleFavorite = async (bookItemId: number): Promise<string> => {
    try {
        const response = await axios.post<string>(`${API_URL}/${bookItemId}`);
        return response.data;
    } catch (error) {
        console.error('도서 좋아요 토글 실패:', error);
        throw error;
    }
};

// 도서의 좋아요 수 조회
export const getBookFavoriteCount = async (bookItemId: number): Promise<number> => {
    try {
        const response = await axios.get<number>(`${API_URL}/count/${bookItemId}`);
        return response.data;
    } catch (error) {
        console.error('도서 좋아요 수 조회 실패:', error);
        throw error;
    }
};

// 내 좋아요 수 조회
export const getUserFavoriteCount = async (): Promise<number> => {
    try {
        const response = await axios.get<number>(`${API_URL}/count`);
        return response.data;
    } catch (error) {
        console.error('내 좋아요 수 조회 실패:', error);
        throw error;
    }
};