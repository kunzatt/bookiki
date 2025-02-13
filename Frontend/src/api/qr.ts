import axios from 'axios'

import type { QrCodeResponse } from '@/types/api/qrCode'

export const createQRCode = async (bookItemId: number): Promise<QrCodeResponse> => {
    try{
        const response = await axios.post<QrCodeResponse>(
            '/api/admin/qrcodes/{bookItemId}',
        );
        return response.data;
    } catch(error){
        console.error('QR 코드 생성 실패: ', error);
        throw error;
    }
};