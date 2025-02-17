import axios from 'axios';
import type {
  BookBorrowResponse,
  BookHistoryResponse,
  BookReturnRequest,
  BookRankingResponse,
  BookHistoryRequest,
} from '@/types/api/bookHistory';
import { PeriodType } from '@/types/enums/periodType';

const API_URL = '/api';

// 관리자용 도서 대출 기록 전체 조회
export const getAdminBookHistories = async (params: BookHistoryRequest): Promise<BookHistoryResponse> => {
  try {
    const response = await axios.get<BookHistoryResponse>(
      `${API_URL}/admin/book-histories`,
      { params }
    );
    return response.data;
  } catch (error) {
    console.error('관리자용 도서 대출 기록 조회 실패:', error);
    throw error;
  }
};

// 사용자용 도서 대출 기록 조회
export const getUserBookHistories = async (params: BookHistoryRequest): Promise<BookHistoryResponse> => {
  try {
    const response = await axios.get<BookHistoryResponse>(
      `${API_URL}/user/book-histories`,
      { params }
    );
    return response.data;
  } catch (error) {
    console.error('사용자용 도서 대출 기록 조회 실패:', error);
    throw error;
  }
};

// 현재 대출 중인 도서 목록 조회
export const getCurrentBorrowedBooks = async (onlyOverdue?: boolean): Promise<BookHistoryResponse[]> => {
  try {
    const response = await axios.get<BookHistoryResponse[]>(
      `${API_URL}/user/book-histories/current`,
      {
        params: { onlyOverdue },
      },
    );
    return response.data;
  } catch (error) {
    console.error('현재 대출 중인 도서 목록 조회 실패:', error);
    throw error;
  }
};

// 도서 대출
export const borrowBook = async (bookItemId: number): Promise<BookBorrowResponse> => {
  try {
    const response = await axios.post<BookBorrowResponse>(
      `${API_URL}/books/borrow`, 
      null, 
      {
        params: { bookItemId },
      }
    );
    return response.data;
  } catch (error) {
    console.error('도서 대출 실패:', error);
    throw error;
  }
};

// 도서 반납 스캔 결과 처리
export const processScanResult = async (request: BookReturnRequest): Promise<void> => {
  try {
    await axios.post(`${API_URL}/books/return/scan`, request);
  } catch (error) {
    console.error('도서 반납 스캔 결과 처리 실패:', error);
    throw error;
  }
};

// 도서 대출 순위 조회
export const getBookRanking = async (): Promise<BookRankingResponse[]> => {
  try {
    const response = await axios.get<BookRankingResponse[]>(
      `${API_URL}/books/ranking`,
    );
    return response.data;
  } catch (error) {
    console.error('도서 대출 순위 조회 실패:', error);
    throw error;
  }
};
