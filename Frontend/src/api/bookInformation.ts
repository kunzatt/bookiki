import axios from 'axios';

const API_URL = '/api/books' // 베이스 URL을 설정합니다.

import type { 
    BookInformationResponse,

} from '@/types/api/bookInformation';

export const getBookInformationByIsbn = async (isbn: string): Promise<BookInformationResponse> => {
    try {
        const response = await axios.get<BookInformationResponse>(`${API_URL}/info/isbn/${isbn}`);
        return response.data;
    } catch (error) {
        console.error('ISBN으로 도서 정보 조회 실패:', error);
        throw error;
    }
};

export const getBookInformation = async  (bookInformationId: Number): Promise<BookInformationResponse> => {
    try {
        const response = await axios.get<BookInformationResponse>(`${API_URL}/info/${bookInformationId}`);
        return response.data;
    } catch (error) {
        console.error('ID로 도서 정보 조회 실패:', error);
        throw error;
    }
 };

