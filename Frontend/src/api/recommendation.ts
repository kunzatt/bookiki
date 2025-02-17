import axios from 'axios';

const API_URL = '/api/recommendations';

import type {
    BookItemListResponse
} from '@/types/api/bookItem';

export interface RecommendationRequest {
    limit?: number;
}

export interface RecommendationResponse {
    recommendations: (BookItemListResponse & { bookItemId: number })[];
    recommendationReason: string;
}

// 도서 추천 조회
export const getRecommendations = async (
    request: RecommendationRequest = { limit: 5 } // 기본값 설정
): Promise<RecommendationResponse> => {
    try {
        const response = await axios.get<RecommendationResponse>(
            `${API_URL}`,
            { params: request }
        );
        console.log('도서 추천 응답:', response.data); // 디버깅을 위한 로그 추가
        return response.data;
    } catch (error) {
        console.error('도서 추천 조회 실패:', error);
        throw error; // 에러를 상위로 전파
    }
};