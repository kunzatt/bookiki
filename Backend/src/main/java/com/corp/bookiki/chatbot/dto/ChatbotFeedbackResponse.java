package com.corp.bookiki.chatbot.dto;

import com.corp.bookiki.chatbot.entity.ChatbotFeedbackEntity;
import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "챗봇 피드백 응답")
public class ChatbotFeedbackResponse {
    @Schema(description = "피드백 ID", example = "1")
    private Integer id;

    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;

    @Schema(description = "이전 대화 의도", example = "qr_troubleshooting")
    private String originalIntent;

    @Schema(description = "피드백 내용", example = "QR 코드가 계속 인식되지 않아요")
    private String feedbackMessage;

    @Schema(description = "피드백 카테고리", example = "QR_ERROR")
    private String category;

    @Schema(description = "처리 상태", example = "PENDING")
    private FeedbackStatus status;

    @Schema(description = "생성일시", example = "2024-02-07T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-02-07T10:00:00")
    private LocalDateTime updatedAt;

    public static ChatbotFeedbackResponse from(ChatbotFeedbackEntity feedback) {
        ChatbotFeedbackResponse response = new ChatbotFeedbackResponse();
        response.id = feedback.getId();
        response.userId = feedback.getUserId();
        response.originalIntent = feedback.getOriginalIntent();
        response.feedbackMessage = feedback.getFeedbackMessage();
        response.category = feedback.getCategory();
        response.status = feedback.getStatus();
        response.createdAt = feedback.getCreatedAt();
        response.updatedAt = feedback.getUpdatedAt();
        return response;
    }
}
