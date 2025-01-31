package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 목록 응답")
public class QnaListResponse {
    @Schema(description = "QnA ID", example = "1")
    private int id;

    @Schema(description = "제목", example = "도서관 이용 문의")
    private String title;

    @Schema(description = "문의사항 유형", example = "도서관 이용")
    private String qnaType;

    @Schema(description = "작성자 이름", example = "테스트계정")
    private String authorName;

    @Schema(description = "생성일시", example = "2024-01-24T10:00:00")
    private LocalDateTime createdAt;

    public QnaListResponse(QnaEntity qna) {
        this.id = qna.getId();
        this.title = qna.getTitle();
        this.qnaType = qna.getQnaType();
        this.authorName = "박성문"; // 임시 테스트용 작성자 이름
        this.createdAt = qna.getCreatedAt();
    }
}