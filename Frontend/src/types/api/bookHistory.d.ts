import { PeriodType } from "../enums/periodType";

// 책 대출 요청
export interface BookBorrowRequest {
    bookItemId: number;
}

// 책 대출 응답
export interface BookBorrowResponse {
    id: number;
    bookItemId: number;
    userId: number;
    borrowedAt: string;
}

// 책 이력 요청
export interface BookHistoryRequest {
    periodType: PeriodType;
    startDate: string;
    endDate: string;
    userName: string;
}

// 책 이력 응답
export interface BookBorrowResponse {
    id: number;
    bookItemId: number;
    userId: number;
    borrowedAt: string;
    returnedAt: string;
    bookTitle: string;
    bookAuthor: string;
    overdue: boolean;
}

// 책 반납 요청
export interface BookReturnRequest {
    scannedBookItemIds: number[];
    ocrResults: string[];
}

// 책 순위 응답
export interface BookRankingResponse {
  /** 도서 아이템 ID */
  bookItemId: number
  /** 도서 제목 */
  title: string
  /** 도서 저자 */
  author: string
  /** 도서 카테고리 코드 */
  category: number
  /** 도서 이미지 URL */
  image: string
  /** 도서 대출 횟수 */
  borrowCount: number
}