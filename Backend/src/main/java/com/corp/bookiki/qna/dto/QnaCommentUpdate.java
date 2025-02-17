package com.corp.bookiki.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의사항 답변 수정 요청")
public class QnaCommentUpdate {
    @Schema(description = "답변 ID", example = "1", required = true)
    private Integer id;

    @Schema(description = "내용", example = "수정된 답변입니다.", required = true)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;
}
