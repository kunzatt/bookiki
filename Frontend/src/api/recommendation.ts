import axios from 'axios';

const API_URL = '/api/recommendations';

import type {
    RecommendationRequest,
    RecommendationResponse
} from '@/types/api/recommendation';

// 도서 추천 조회
export const getRecommendations = async (
    request: RecommendationRequest = { limit: 5 } // 기본값 설정
): Promise<RecommendationResponse> => {
    try {
        const response = await axios.get<RecommendationResponse>(
            `${API_URL}`,
            { params: request }
        );
        return response.data;
    } catch (error) {
        console.error('도서 추천 조회 실패:', error);
        throw error;
    }
};