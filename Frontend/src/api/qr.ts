import axios from 'axios';

import type { QrCodeResponse } from '@/types/api/qrCode';

/**
 * 도서 ID를 이용하여 새로운 QR 코드를 생성합니다.
 * @param bookItemId - 도서 아이템 ID
 */
export const createQRCode = async (bookItemId: number): Promise<QrCodeResponse> => {
    try {
        const response = await axios.post<QrCodeResponse>(
            `/api/admin/qrcodes/${bookItemId}`
        );
        return response.data;
    } catch (error) {
        console.error('QR 코드 생성 실패:', error);
        throw error;
    }
};