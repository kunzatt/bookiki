import axios from 'axios';

const API_URL = '/api' // 베이스 URL을 설정합니다.

import type { 
    ChatbotRequest,
    ChatbotResponse,
    ChatbotFeedbackRequest,
    ChatbotFeedbackResponse,
    UpdateFeedbackStatusRequest
} from '@/types/api/chatbot';

import { FeedbackStatus } from '@/types/enums/feedbackStatus'
import type { PageResponse } from '@/types/common/pagination'

// 챗봇 메시지 전송
export const sendMessage = async (request: ChatbotRequest) => {
    try {
        const response = await axios.post<ChatbotResponse>(
            `${API_URL}/chatbot/message`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('챗봇 메시지 전송 실패:', error);
        throw error;
    }
};

// 챗봇 피드백 전송
export const sendFeedback = async (request: ChatbotFeedbackRequest): Promise<string> => {
    try {
        const response = await axios.post<string>(
            `${API_URL}/chatbot/feedback`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('챗봇 피드백 전송 실패:', error);
        throw error;
    }
 };

 // 피드백 목록 조회
export const selectFeedbacks = async (
    status?: FeedbackStatus,
    category?: string,
    startDate?: string,
    endDate?: string,
    page: number = 0,
    size: number = 20
 ): Promise<PageResponse<ChatbotFeedbackResponse>> => {
    try {
        const response = await axios.get<PageResponse<ChatbotFeedbackResponse>>(`${API_URL}/admin/feedback`, {
            params: {
                status,
                category,
                startDate,
                endDate,
                page,
                size
            }
        });
        return response.data;
    } catch (error) {
        console.error('피드백 목록 조회 실패:', error);
        throw error;
    }
 };

 // 특정 피드백 조회
export const selectFeedbackById = async (id: number): Promise<ChatbotFeedbackResponse> => {
    try {
        const response = await axios.get<ChatbotFeedbackResponse>(`${API_URL}/admin/feedback/${id}`);
        return response.data;
    } catch (error) {
        console.error('피드백 상세 조회 실패:', error);
        throw error;
    }
 };
 
 // 피드백 상태 업데이트
 export const updateFeedbackStatus = async (
    id: number, 
    request: UpdateFeedbackStatusRequest
 ): Promise<ChatbotFeedbackResponse> => {
    try {
        const response = await axios.patch<ChatbotFeedbackResponse>(
            `${API_URL}/admin/feedback/${id}/status`,
            request
        );
        return response.data;
    } catch (error) {
        console.error('피드백 상태 업데이트 실패:', error);
        throw error;
    }
 };
 
 // 사용자별 피드백 조회
 export const selectFeedbackByUserId = async (userId: number): Promise<ChatbotFeedbackResponse[]> => {
    try {
        const response = await axios.get<ChatbotFeedbackResponse[]>(
            `${API_URL}/admin/feedback/user/${userId}`
        );
        return response.data;
    } catch (error) {
        console.error('사용자별 피드백 조회 실패:', error);
        throw error;
    }
 };