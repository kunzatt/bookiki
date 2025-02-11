import axios from 'axios';

const API_URL = '/api' // 베이스 URL을 설정합니다.

import type { 
    BookHistoryRequest, 
    BookBorrowRequest, 
    BookBorrowResponse,
    BookReturnRequest 
} from '@/types/api/bookHistory';

import { PeriodType } from '@/types/enums/periodType';
import type { PageResponse } from '@/types/common/pagination';

// 관리자용 도서 대출 기록 전체 조회
export const getAdminBookHistories = async (
    periodType: PeriodType,
    startDate?: string,
    endDate?: string,
    userName?: string,
    companyId?: string,
    overdue?: boolean,
    page: number = 0,
    size: number = 20
) => {
    try {
        const response = await axios.get<PageResponse<BookBorrowResponse>>(`${API_URL}/admin/book-histories`, {
            params: {
                periodType,
                startDate,
                endDate,
                userName,
                companyId,
                overdue,
                page,
                size
            }
        });
        return response.data;
    } catch (error) {
        console.error('관리자용 도서 대출 기록 조회 실패:', error);
        throw error;
    }
};

// 사용자용 도서 대출 기록 조회
export const getUserBookHistories = async (
    request: BookHistoryRequest,
    overdue?: boolean,
    page: number = 0,
    size: number = 20
) => {
    try {
        const response = await axios.get<PageResponse<BookBorrowResponse>>(`${API_URL}/user/book-histories`, {
            params: {
                ...request,
                overdue,
                page,
                size
            }
        });
        return response.data;
    } catch (error) {
        console.error('사용자용 도서 대출 기록 조회 실패:', error);
        throw error;
    }
};

// 현재 대출 중인 도서 목록 조회
export const getCurrentBorrowedBooks = async (onlyOverdue?: boolean) => {
    try {
        const response = await axios.get<BookBorrowResponse[]>(`${API_URL}/user/book-histories/current`, {
            params: {
                onlyOverdue
            }
        });
        return response.data;
    } catch (error) {
        console.error('현재 대출 중인 도서 목록 조회 실패:', error);
        throw error;
    }
};

// 사용자 도서 대출
export const borrowBook = async (bookItemId: number): Promise<BookBorrowResponse> => {
    try {
        const response = await axios.post<BookBorrowResponse>(`${API_URL}/books/borrow`, null, {
            params: {
                bookItemId
            }
        });
        return response.data;
    } catch (error) {
        console.error('도서 대출 요청 실패:', error);
        throw error;
    }
 };

 // 도서 반납
 export const processScanResult = async (request: BookReturnRequest): Promise<void> => {
    try {
        await axios.post(`${API_URL}/books/return/scan`, request);
    } catch (error) {
        console.error('도서 반납 스캔 처리 실패:', error);
        throw error;
    }
 };