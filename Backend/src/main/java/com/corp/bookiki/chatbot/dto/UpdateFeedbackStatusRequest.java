package com.corp.bookiki.chatbot.dto;

import com.corp.bookiki.chatbot.entity.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "피드백 상태 업데이트 요청")
public class UpdateFeedbackStatusRequest {
    @Schema(description = "새로운 상태", example = "IN_PROGRESS")
    @NotNull(message = "상태는 필수 입력값입니다")
    private FeedbackStatus status;

    @Builder
    public UpdateFeedbackStatusRequest(FeedbackStatus status) {
        this.status = status;
    }
}
