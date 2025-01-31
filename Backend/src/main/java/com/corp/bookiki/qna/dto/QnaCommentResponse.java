package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaCommentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 답변 응답")
public class QnaCommentResponse {
    @Schema(description = "답변 ID", example = "1")
    private int id;

    @Schema(description = "문의사항 ID", example = "1")
    private int qnaId;

    @Schema(description = "내용", example = "안녕하세요. 도서관 운영시간은 평일 9시부터 18시까지입니다.")
    private String content;

    @Schema(description = "작성자 ID", example = "1")
    private int authorId;

    @Schema(description = "생성일시", example = "2024-01-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-24T10:00:00")
    private LocalDateTime updatedAt;

    public QnaCommentResponse(QnaCommentEntity comment) {
        this.id = comment.getId();
        this.qnaId = comment.getQnaId();
        this.content = comment.getContent();
        this.authorId = comment.getAuthorId();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
