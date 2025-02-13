import axios from 'axios';
import type { IotMessage } from '@/types/api/iot';

const API_URL = '/api/ws';

// 책 위치 LED 표시 요청
export const requestBookLocation = async (bookItemId: number): Promise<IotMessage> => {
    try {
        const response = await axios.get<IotMessage>(
            `${API_URL}/book-location/${bookItemId}`
        );
        return response.data;
    } catch (error) {
        console.error('책 위치 LED 표시 요청 실패:', error);
        throw error;
    }
};