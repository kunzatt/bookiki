package com.corp.bookiki.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 수정 요청")
public class QnaUpdate {
    @Schema(description = "문의사항 ID", example = "1", required = true)
    private int id;

    @Schema(description = "제목", example = "도서관 이용 문의 (수정)", required = true)
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @Schema(description = "내용", example = "수정된 문의 내용입니다.", required = true)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;
}
