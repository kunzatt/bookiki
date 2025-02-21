import axios from 'axios';
import type { NoticeRequest, NoticeResponse, NoticeUpdate } from '@/types/api/notice';

const API_URL = import.meta.env.VITE_API_URL;

// axios 인스턴스 생성 - HttpOnly 쿠키 사용을 위한 설정
const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  withCredentials: true, // 쿠키를 포함한 크로스 도메인 요청 허용
});

// 공지사항 등록
export const createNotice = async (request: NoticeRequest): Promise<number> => {
  console.log('createNotice called with:', request);
  console.log('API_URL:', API_URL);

  try {
    // API 요청
    const response = await axiosInstance.post<number>('/api/admin/notices', request);
    console.log('API response:', response);
    return response.data;
  } catch (error) {
    console.error('API error in createNotice:', error);
    if (error.response) {
      console.error('Error response:', error.response.data);
      console.error('Error status:', error.response.status);
    } else if (error.request) {
      console.error('Error request:', error.request);
    } else {
      console.error('Error message:', error.message);
    }
    throw error;
  }
};

// 공지사항 목록 조회
export const selectAllNotices = async (params: {
  keyword?: string;
  pageable?: {
    page?: number;
    size?: number;
    sort?: string[]; // string[] 타입으로 변경
  };
}) => {
  try {
    const searchParams = new URLSearchParams();

    if (params.keyword) {
      searchParams.append('keyword', params.keyword);
    }

    if (params.pageable) {
      searchParams.append('page', String(params.pageable.page || 0));
      searchParams.append('size', String(params.pageable.size || 10));
      // sort 처리 방식 변경
      if (params.pageable.sort && params.pageable.sort.length > 0) {
        params.pageable.sort.forEach((sort) => {
          searchParams.append('sort', sort);
        });
      }
    }

    const url = `/api/notices?${searchParams.toString()}`;
    console.log('Request URL:', url); // 요청 URL 로깅

    const response = await axiosInstance.get(url);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        // 인증 실패시 로그인 페이지로 리다이렉트
        window.location.href = '/login';
      }
    }
    console.error('공지사항 목록 조회 실패:', error);
    throw error;
  }
};

// 공지사항 상세 조회
export const selectNoticeById = async (id: number): Promise<NoticeResponse> => {
  const response = await axiosInstance.get<NoticeResponse>(`/api/notices/${id}`);
  return response.data;
};

// 공지사항 삭제
export const deleteNotice = async (id: number): Promise<void> => {
  await axiosInstance.delete(`/api/admin/notices/${id}`);
};

// 공지사항 수정
export const updateNotice = async (update: NoticeUpdate): Promise<string> => {
  const response = await axiosInstance.put<string>(`/api/admin/notices`, update);
  return response.data;
};
