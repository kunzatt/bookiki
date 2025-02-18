// utils/qrCode.ts
import QRCode from 'qrcode';

export const generateQRImage = async (value: string): Promise<string> => {
  try {
    // QR 코드를 Canvas에 생성
    const qrCanvas = await QRCode.toCanvas(value, {
      width: 200,
      margin: 2,
      errorCorrectionLevel: 'H',
    });

    // Canvas를 base64 이미지로 변환
    return qrCanvas.toDataURL('image/png');
  } catch (error) {
    console.error('QR 코드 이미지 생성 실패:', error);
    throw error;
  }
};
