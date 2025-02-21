package com.corp.bookiki.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사용자 추가 문의")
public class ChatbotFeedbackRequest {
    @Schema(description = "이전 대화 의도", example = "qr_troubleshooting")
    private String originalIntent;    // 어떤 대화 맥락에서 발생했는지

    @Schema(description = "추가 문의 내용", example = "QR 코드가 계속 인식되지 않아요")
    @NotBlank(message = "문의 내용은 필수 입력값입니다")
    private String feedbackMessage;   // 사용자의 추가 문의사항

    @Schema(description = "문의 유형", example = "QR_ERROR")
    private String category;          // 문의 유형

    @Builder
    public ChatbotFeedbackRequest(String originalIntent, String feedbackMessage, String category) {
        this.originalIntent = originalIntent;
        this.feedbackMessage = feedbackMessage;
        this.category = category;
    }
}
