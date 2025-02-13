import axios from 'axios'

const API_URL = '/api/qna' // 베이스 URL을 설정합니다.

import type {
    QnaRequest,
    QnaListResponse,
    QnaDetailResponse,
    QnaUpdate, 
} from '@/types/api/qna'
import type { PageResponse } from '@/types/common/pagination';

 //문의사항 등록
 export const createQna = async (request: QnaRequest): Promise<Number> => {
    try{
        const response = await axios.post<Number>(
            `${API_URL}`,
            request
        );
         return response.data;
    } catch(error){
        console.error('문의사항 등록 실패: ', error);
        throw error;
    }
};

//문의사항 목록 조회
export const selectQnas = async (
    keyword?: string,
    qnaType?: string,
    answered?: boolean,
    page: number = 0,
    size: number = 10
): Promise<PageResponse<QnaListResponse>> => {
    try{
        const response = await axios.get<PageResponse<QnaListResponse>>(`${API_URL}/qnas`, {
            params: {
                keyword,
                qnaType,
                answered,
                page,
                size
            }
        });
        return response.data;
    } catch(error){
        console.error('문의사항 목록 조회 실패:', error);
        throw error;
    }
};

// 문의사항 1개 상세조회
export const selectQnaById = async (id: number): Promise<QnaDetailResponse> => {
    try{
        const response = await axios.get<QnaDetailResponse>(`${API_URL}/{id}`);
        return response.data;
    }catch(error){
        console.error('문의사항 상세 조회 실패: ', error);
        throw error;
    }
};

// 문의사항 삭제
export const deleteQna = async (id: number): Promise<string> => {
    try{
        const response = await axios.delete<string>(`${API_URL}/${id}`);
        return response.data;
    }catch(error){
        console.error('문의사항 삭제 실패: ', error);
        throw error;
    }
};

// 문의사항 수정
export const updateQna = async (update: QnaUpdate): Promise<string> => {
    try{
        const response = await axios.put<string>(`${API_URL}`, update);
        return response.data;
    }catch(error){
        console.error('문의사항 수정 실패: ', error);
        throw error;
    }
};
