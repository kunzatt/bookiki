import axios from 'axios'

const API_URL = '/api' // 베이스 URL을 설정합니다.

import type {
  NoticeRequest,
  NoticeResponse,
  NoticeUpdate
 } from '@/types/api/notice'
 import type { PageResponse } from '@/types/common/pagination';

// 공지사항 등록
export const createNotice = async (request: NoticeRequest): Promise<Number> => {
  try{
    const response = await axios.post<Number>(
      `${API_URL}/admin/notices`,
      request
    );
    return response.data;
  } catch(error){
    console.error('공지사항 등록 실패:', error);
    throw error;
  }
};

// 공지사항 목록 조회
export const selectAllNotices = async (
  keyword?: string,
  page: number = 0,
  size: number = 10
): Promise<PageResponse<NoticeResponse>> => {
  try {
      const response = await axios.get<PageResponse<NoticeResponse>>(`${API_URL}/notices`, {
          params: {
              keyword,
              page,
              size
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
export const deleteNotice = async (id: number): Promise<string> => {
  try {
      const response = await axios.delete<string>(`${API_URL}/admin/notices/${id}`);
      return response.data;
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
