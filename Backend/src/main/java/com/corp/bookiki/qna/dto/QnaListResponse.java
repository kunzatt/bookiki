package com.corp.bookiki.qna.dto;

import com.corp.bookiki.qna.entity.QnaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "문의사항 목록 응답")
public class QnaListResponse {
    @Schema(description = "QnA ID", example = "1")
    private Integer id;

    @Schema(description = "제목", example = "도서관 이용 문의")
    private String title;

    @Schema(description = "문의사항 유형", example = "도서관 이용")
    private String qnaType;

    @Schema(description = "작성자 이름", example = "테스트계정")
    private String authorName;

    @Schema(description = "생성일시", example = "2024-01-24T10:00:00")
    private LocalDateTime createdAt;

    // 페이지 메타 정보
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;

    public QnaListResponse(QnaEntity qna, String authorName) {
        this.id = qna.getId();
        this.title = qna.getTitle();
        this.qnaType = qna.getQnaType();
        this.authorName = authorName;
        this.createdAt = qna.getCreatedAt();
    }

    public void setPageInfo(Page<?> page) {
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

}