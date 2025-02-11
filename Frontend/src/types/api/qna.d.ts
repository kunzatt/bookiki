// 문의사항 답변 요청
export interface QnaCommentRequest {
    qnaId: number;
    content: string;
}

// 문의사항 답변 응답
export interface QnaCommentResponse {
    id: number;
    qnaId: number;
    content: string;
    authorId: number;
    createdAt: string;
    updatedAt: string;
}

// 문의사항 답변 수정
export interface QnaCommentUpdate {
    id: number;
    content: string;
}

// 문의사항 상세 응답
export interface QnaDetailResponse {
    id: number;
    title: string;
    qnaType: string;
    content: string;
    authorId: number;
    authorName: string;
    createdAt: string;
    updatedAt: string;
    comments: QnaCommentResponse[];
}

// 문의사항 리스트 응답
export interface QnaListResponse {
    id: number;
    title: string;
    qnaType: string;
    quthorName: string;
    createdAt: string;
}

// 문의사항 요청
export interface QnaRequest {
    title: string;
    qnaType: string;
    content: string;
}

// 문의사항 응답
export interface QnaUpdate {
    id: number;
    title: string;
    qnaType: string;
    content: string;
}