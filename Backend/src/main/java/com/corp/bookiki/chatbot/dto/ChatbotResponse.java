package com.corp.bookiki.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "챗봇 응답")
public class ChatbotResponse {
    @Schema(description = "챗봇 응답 메시지")
    private String message;

    @Schema(description = "관리자 문의 버튼 표시 여부")
    private boolean showAdminInquiryButton;

    @Builder
    public ChatbotResponse(String message, boolean showAdminInquiryButton) {
        this.message = message;
        this.showAdminInquiryButton = showAdminInquiryButton;
    }
}
