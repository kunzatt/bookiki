import { Category } from "../enums/category";

// 책장 생성 요청
export interface ShelfCreateRequest {
    shelfNumber: number;
    lineNumber: number;
    category: Category;
}

// 책장 응답
export interface ShelfResponse {
    id: number;
    shelfNumber: number;
    lineNumber: number;
    category: Category;
}

// 책장 수정 요청
export interface ShelfUpdateRequest {
    id: number;
    shelfNumber: number;
    lineNumber: number;
    category: Category;
}