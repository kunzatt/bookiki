import axios from 'axios';

const API_URL = '/api/books';

import type { BookInformationResponse } from '@/types/api/bookInformation';

/**
 * ISBN으로 도서 정보를 조회합니다.
 * @param isbn - 조회할 도서의 ISBN (e.g. "9788937460470")
 */
export const getBookInformationByIsbn = async (isbn: string): Promise<BookInformationResponse> => {
    try {
        const response = await axios.get<BookInformationResponse>(`${API_URL}/info/isbn/${isbn}`);
        return response.data;
    } catch (error) {
        console.error('ISBN으로 도서 정보 조회 실패:', error);
        throw error;
    }
};

/**
 * ID로 도서 정보를 조회합니다.
 * @param bookInformationId - 조회할 도서의 ID
 */
export const getBookInformation = async (
    bookInformationId: number  // Number -> number로 변경
): Promise<BookInformationResponse> => {
    try {
        const response = await axios.get<BookInformationResponse>(
            `${API_URL}/info/${bookInformationId}`
        );
        return response.data;
    } catch (error) {
        console.error('ID로 도서 정보 조회 실패:', error);
        throw error;
    }
};