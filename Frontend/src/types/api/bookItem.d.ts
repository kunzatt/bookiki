import { BookStatus } from '../enums/bookStatus';
import { QrCodeResponse } from './qrCode';

// 가상책장 응답
export interface BookItemDisplayResponse {
  id: number;
  image: string; // 이미지 url
}

// 책 리스트 응답
export interface BookItemListResponse {
  id: number;
  image: string;
  title: string;
  author: string;
}

// 책 요청
export interface BookItemRequest {
  bookInformationId: number;
  purchaseAt: string;
}

// 책 응답
export interface BookItemResponse {
  id: number;
  bookInformationId: number;
  purchaseAt: string;
  bookStatus: BookStatus;
  updatedAt: string;
  qrCode: QrCodeResponse;
}
interface BookAdminListResponse {
  id: number;
  title: string;
  isbn: string;
  category: Category; // enum은 별도로 타입 정의 필요
  bookStatus: BookStatus; // enum은 별도로 타입 정의 필요
  qrCode: Integer | null;
}
