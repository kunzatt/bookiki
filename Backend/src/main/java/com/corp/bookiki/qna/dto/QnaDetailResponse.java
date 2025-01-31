package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 상세 응답")
public class QnaDetailResponse {
    @Schema(description = "QnA ID", example = "1")
    private int id;

    @Schema(description = "제목", example = "도서관 이용 문의")
    private String title;

    @Schema(description = "문의사항 유형", example = "도서관 이용")
    private String qnaType;

    @Schema(description = "내용", example = "도서관 이용 시간이 어떻게 되나요?")
    private String content;

    @Schema(description = "작성자 ID", example = "1")
    private int authorId;

    // entity에는 없지만, 함께 사용해야할 column
    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "생성일시", example = "2024-01-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-24T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "댓글 목록")
    private List<QnaCommentResponse> comments;

    public QnaDetailResponse(QnaEntity qna, String authorName, List<QnaCommentResponse> comments) {
        this.id = qna.getId();
        this.title = qna.getTitle();
        this.qnaType = qna.getQnaType();
        this.content = qna.getContent();
        this.authorId = qna.getAuthorId();
        this.authorName = authorName;
        this.createdAt = qna.getCreatedAt();
        this.updatedAt = qna.getUpdatedAt();
        this.comments = comments;
    }
}
