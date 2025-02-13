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
export interface BookHistoryResponse {
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
  bookItemId: number
  title: string
  author: string
  category: number
  image: string
  borrowCount: number
}