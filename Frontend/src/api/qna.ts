import axios from 'axios';

const API_URL = '/api';

import type {
  QnaRequest,
  QnaListResponse,
  QnaDetailResponse,
  QnaUpdate,
  QnaCommentRequest,
  QnaCommentUpdate,
} from '@/types/api/qna';
import type { PageResponse } from '@/types/common/pagination';

// QNA API
export const createQna = async (request: QnaRequest): Promise<number> => {
  try {
    const response = await axios.post<number>(`${API_URL}/qna`, request);
    return response.data;
  } catch (error) {
    console.error('문의사항 등록 실패:', error);
    throw error;
  }
};

export const selectQnas = async (params: {
  keyword?: string;
  qnaType?: string;
  pageable?: {
    page?: number;
    size?: number;
    sort?: string[];
  };
}) => {
  try {
    const searchParams = new URLSearchParams();

    if (params.keyword) {
      searchParams.append('keyword', params.keyword);
    }
    if (params.qnaType) {
      searchParams.append('qnaType', params.qnaType);
    }

    if (params.pageable) {
      searchParams.append('page', String(params.pageable.page || 0));
      searchParams.append('size', String(params.pageable.size || 10));
      if (params.pageable.sort && params.pageable.sort.length > 0) {
        params.pageable.sort.forEach((sort) => {
          searchParams.append('sort', sort);
        });
      }
    }

    const url = `/api/qna?${searchParams.toString()}`;
    console.log('Request URL:', url);

    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        window.location.href = '/login';
      }
    }
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
    const response = await axios.post<number>(`${API_URL}/admin/qna`, request);
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
    const response = await axios.put<string>(`${API_URL}/admin/qna`, update);
    return response.data;
  } catch (error) {
    console.error('문의사항 답변 수정 실패:', error);
    throw error;
  }
};
