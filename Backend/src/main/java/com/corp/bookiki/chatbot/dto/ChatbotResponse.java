package com.corp.bookiki.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "챗봇 응답")
public class ChatbotResponse {
    @Schema(description = "챗봇 응답 메시지")
    private String message;

    @Schema(description = "빠른 응답 선택지")
    private List<String> quickReplies;

    @Schema(description = "자유 입력 허용 여부")
    private boolean allowFreeInput;

    @Schema(description = "피드백 버튼 표시 여부")
    private boolean showAdminInquiryButton;

    @Builder
    public ChatbotResponse(String message, List<String> quickReplies,
                           boolean allowFreeInput, boolean showAdminInquiryButton) {
        this.message = message;
        this.quickReplies = quickReplies != null ? quickReplies : new ArrayList<>();
        this.allowFreeInput = allowFreeInput;
        this.showAdminInquiryButton = showAdminInquiryButton;
    }
}
