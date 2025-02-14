import axios, { type AxiosError } from 'axios';
import instance from './axios';

const API_URL = '/api/favorites';

import type { BookFavoriteResponse } from '@/types/api/favorite';
import type { PageResponse } from '@/types/common/pagination';

// 도서 좋아요 여부 확인
export const checkFavorite = async (bookItemId: number): Promise<boolean> => {
  try {
    const response = await instance.get<boolean | string>(`${API_URL}/${bookItemId}`);
    console.log('[checkFavorite] API Response:', response.data);

    // response.data의 타입에 따른 처리
    if (typeof response.data === 'string') {
      return response.data === 'true';
    }

    return Boolean(response.data); // 명시적으로 boolean으로 변환
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        throw error;
      }
      console.error('[checkFavorite] Error:', error);
    }
    throw error; // 에러를 상위로 전파하여 컴포넌트에서 처리
  }
};

// 내 좋아요 목록 조회
export const getUserFavorites = async (
  page: number = 0,
  size: number = 20,
  sort?: string,
): Promise<PageResponse<BookFavoriteResponse>> => {
  try {
    const response = await instance.get<PageResponse<BookFavoriteResponse>>(`${API_URL}`, {
      params: { page, size, sort },
    });
    return response.data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      console.error('좋아요 목록 조회 실패:', error);
    }
    throw error;
  }
};

// 도서 좋아요 토글
export const toggleFavorite = async (bookItemId: number): Promise<string> => {
  try {
    console.log('[toggleFavorite] Request for bookId:', bookItemId);
    const response = await instance.post<string>(`${API_URL}/${bookItemId}`);
    console.log('[toggleFavorite] Response:', response.data);
    return response.data;
  } catch (error) {
    console.error('[toggleFavorite] Error:', error);
    throw error;
  }
};

// 도서의 좋아요 수 조회
export const getBookFavoriteCount = async (bookItemId: number): Promise<number> => {
  try {
    console.log('API 호출: 좋아요 수 조회', bookItemId);
    const response = await instance.get<number>(`${API_URL}/count/${bookItemId}`);
    console.log('API 응답: 좋아요 수', response.data);
    return response.data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      console.error('도서 좋아요 수 조회 실패:', error);
    }
    throw error;
  }
};

// 내 좋아요 수 조회
export const getUserFavoriteCount = async (): Promise<number> => {
  try {
    const response = await instance.get<number>(`${API_URL}/count`);
    return response.data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      console.error('내 좋아요 수 조회 실패:', error);
    }
    throw error;
  }
};
