package com.corp.bookiki.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "챗봇 요청")
public class ChatbotRequest {
    @Schema(description = "사용자 입력 메시지", example = "도서관 이용시간 알려줘")
    @NotBlank(message = "메시지는 필수 입력값입니다.")
    private String message;

    @Builder
    public ChatbotRequest(String message) {
        this.message = message;
    }
}
