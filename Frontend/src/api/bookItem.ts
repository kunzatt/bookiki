import axios from 'axios';

const API_URL = '/api';

import type { 
    BookItemListResponse, 
    BookItemDisplayResponse, 
    BookItemRequest, 
    BookItemResponse,
} from '@/types/api/bookItem';
import { SearchType } from '@/types/enums/searchType';
import type { PageResponse } from '@/types/common/pagination';

// 도서 아이템 목록 검색
export const selectBooks = async (
    page: number = 0,
    size: number = 10,
    type: SearchType,
    keyword?: string
): Promise<PageResponse<BookItemListResponse>> => {
    try {
        const response = await axios.get<PageResponse<BookItemListResponse>>(`${API_URL}/books/search`, {
            params: { page, size, type, keyword }
        });
        return response.data;
    } catch (error) {
        console.error('도서 목록 검색 실패:', error);
        throw error;
    }
};

// 도서 아이템 목록 조회 (키워드 검색)
export const selectBooksByKeyword = async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    direction: 'asc' | 'desc' = 'desc',
    keyword?: string
): Promise<PageResponse<BookItemDisplayResponse>> => {
    try {
        const response = await axios.get<PageResponse<BookItemDisplayResponse>>(`${API_URL}/books/search/list`, {
            params: { page, size, sortBy, direction, keyword }
        });
        return response.data;
    } catch (error) {
        console.error('도서 목록 조회 실패:', error);
        throw error;
    }
};

// 도서 아이템 ID로 조회
export const getBookItemById = async (id: number): Promise<BookItemResponse> => {
    try {
        const response = await axios.get<BookItemResponse>(`${API_URL}/books/search/qrcodes/${id}`);
        return response.data;
    } catch (error) {
        console.error('도서 아이템 조회 실패:', error);
        throw error;
    }
};

// 도서 아이템 삭제
export const deleteBookItem = async (id: number): Promise<BookItemResponse> => {
    try {
        const response = await axios.delete<BookItemResponse>(`${API_URL}/books/search/${id}`);
        return response.data;
    } catch (error) {
        console.error('도서 아이템 삭제 실패:', error);
        throw error;
    }
};

// 도서 아이템 등록
export const addBookItem = async (bookItemRequest: BookItemRequest): Promise<BookItemResponse> => {
    try {
        const response = await axios.post<BookItemResponse>(
            `${API_URL}/admin/books/search`, 
            bookItemRequest
        );
        return response.data;
    } catch (error) {
        console.error('도서 아이템 등록 실패:', error);
        throw error;
    }
};