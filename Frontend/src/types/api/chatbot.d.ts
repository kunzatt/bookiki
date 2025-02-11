import { FeedbackStatus } from "../enums/feedbackStatus";

// 챗봇 피드백 요청
export interface ChatbotFeedbackRequest {
    originalIntent: string;
    feedbackMessage: string;
    category: string;
}

// 챗봇 피드백 응답
export interface ChatbotFeedbackResponse {
    id: number;
    userId: number;
    originalIntent: string;
    feedbackMessage: string;
    category: string;
    status: FeedbackStatus;
    createAt: string;
    updatedAt: string;
}

// 챗봇 요청
export interface ChatbotRequest {
    message: string;
}

// 챗봇 응답
export interface ChatbotResponse {
    message: string;
    showAdminInquiryButton: boolean;
}

// 피드백 상태 수정 요청
export interface UpdateFeedbackStatusRequest {
    status: UpdateFeedbackStatusRequest;
}