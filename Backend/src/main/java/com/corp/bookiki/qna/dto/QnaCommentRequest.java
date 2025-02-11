package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaCommentEntity;
import com.corp.bookiki.qna.entity.QnaEntity;
import com.corp.bookiki.user.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 답변 등록 요청")
public class QnaCommentRequest {
    @Schema(description = "문의사항 ID", example = "1", required = true)
    private Integer qnaId;

    @Schema(description = "내용", example = "안녕하세요. 도서관 운영시간은 평일 9시부터 18시까지입니다.", required = true)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    public QnaCommentEntity toEntity(QnaEntity qna) {
        return QnaCommentEntity.builder()
                .qna(qna)
                .content(content)
                .user(qna.getUser())
                .build();
    }
}
