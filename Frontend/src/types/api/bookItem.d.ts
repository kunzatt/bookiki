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

interface QrCodeInfo {
  id: number;
  qrValue: string;
  createAt: string;
}

interface BorrowerInfo {
  userId: number;
  userName: string;
  borrowedAt: string; // LocalDateTime은 문자열로 받습니다
}

export interface BookAdminDetailResponse {
  // 책 정보
  title: string;
  author: string;
  publisher: string;
  isbn: string;
  publishedAt: string; // LocalDateTime
  image: string;
  description: string;
  category: Category;

  // 도서 아이템 정보
  id: number;
  purchaseAt: string; // LocalDateTime
  bookStatus: BookStatus;

  // QR 코드 정보
  qrCode: string | null;

  // 현재 대출자 정보 (optional)
  currentBorrower?: BorrowerInfo;
}
