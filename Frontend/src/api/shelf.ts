import axios from 'axios'

const API_URL = '/api/admin/shelf/categories' // 베이스 URL을 설정합니다.

import type {
    ShelfResponse,
    ShelfCreateRequest,
    ShelfUpdateRequest,

} from '@/types/api/shelf'

// 책장 전체 조회
export const selectAllShelf = async (): Promise<ShelfResponse[]> => {
    try {
        const response = await axios.get<ShelfResponse[]>(
            `${API_URL}`
        )
        return response.data;
    } catch(error){
        console.error('책장 전체 조회 실패: ', error);
        throw error;
    }
};

// 책장 생성
export const createShelf = async(Request: ShelfCreateRequest): Promise<number> => {
    try {
        const response = await axios.post<number>(
            `${API_URL}`,
            Request
        );
        return response.data;
    } catch(error){
        console.error('책장 생성 실패: ', error);
        throw error
    }
};

// 책장 수정
export const updateShelf = async (request: ShelfUpdateRequest): Promise<void> => {
    try {
        await axios.put(
            `${API_URL}`,
            request
        );
    } catch (error) {
        console.error('책장 수정 실패: ', error);
        throw error;
    }
};

// 책장 삭제
export const deleteShelf = async (id: number): Promise<void> => {
    try {
        await axios.delete(`${API_URL}/${id}`);
    } catch (error) {
        console.error('책장 삭제 실패: ', error);
        throw error;
    }
};