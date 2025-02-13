// 좋아요 요청
export interface BookFavoriteRequest {
    bookItemId: number;
}

// 좋아요 응답
export interface BookFavoriteResponse {
    id: number;
    bookItemId: number;
    userId: number;
    bookTitle: string;
    bookImage: string;
    createdAt: string;
}