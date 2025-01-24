package com.corp.bookiki.notice.dto;

import com.corp.bookiki.notice.entity.NoticeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "공지사항 응답")
public class NoticeResponse {
    @Schema(description = "공지사항 ID", example = "1")
    private int id;

    @Schema(description = "제목", example = "시스템 점검 안내")
    private String title;

    @Schema(description = "내용", example = "2024년 1월 25일 오전 2시부터 4시까지 시스템 점검이 진행됩니다.")
    private String content;

    @Schema(description = "생성일시", example = "2024-01-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-24T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "조회수", example = "100")
    private int viewCount;

    // ResponseDTO는 단순 데이터 전달이 목적이기 때문에 Builder보다는 기본 생성자 사용
    // @Builder는 Entity나 RequestDTO에서 사용
    public NoticeResponse(NoticeEntity noticeEntity) {
        this.id = noticeEntity.getId();
        this.title = noticeEntity.getTitle();
        this.content = noticeEntity.getContent();
        this.createdAt = noticeEntity.getCreatedAt();
        this.updatedAt = noticeEntity.getUpdatedAt();
        this.viewCount = noticeEntity.getViewCount();
    }
}
