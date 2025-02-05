package com.corp.bookiki.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@Schema(description = "챗봇 응답")
public class ChatbotResponse {
    @Schema(description = "주요 응답 메시지")
    private String message;

    @Schema(description = "후속 메시지")
    private String followUpMessage;

    @Schema(description = "빠른 응답 선택지")
    private List<String> quickReplies;

    @Schema(description = "의도(intent) 식별자")
    private String intent;

    @Schema(description = "감지된 엔티티들")
    private Map<String, String> entities;

    @Schema(description = "신뢰도 점수")
    private float confidenceScore;

    @Schema(description = "추가 작업 필요 여부")
    private boolean requiresFollowUp;

    @Builder
    public ChatbotResponse(String message, String followUpMessage, List<String> quickReplies,
                           String intent, Map<String, String> entities, float confidenceScore,
                           boolean requiresFollowUp) {
        this.message = message;
        this.followUpMessage = followUpMessage;
        this.quickReplies = quickReplies != null ? quickReplies : new ArrayList<>();
        this.intent = intent;
        this.entities = entities != null ? entities : new HashMap<>();
        this.confidenceScore = confidenceScore;
        this.requiresFollowUp = requiresFollowUp;
    }
}
