
// 추천 요청
export interface RecommendationRequest {
    limit: number;
}

// 추천 응답
export interface RecommendationResponse {
    recommendations: RecommendedBook[];
    recommendationReason: string;
}

// 추천 책
export interface RecommendedBook{
    bookItemId: number;
    title: string;
    author: string;
}