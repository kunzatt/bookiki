// QR 요청
export interface QrCodeRequest {
    bookItemId: number;
}

// QR 응답
export interface QrCodeResponse {
    id: number;
    qrValue: string;
    createAt: string;
}
