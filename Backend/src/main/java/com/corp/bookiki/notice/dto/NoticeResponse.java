package com.corp.bookiki.notice.dto;

import com.corp.bookiki.notice.entity.NoticeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NoticeResponse {
    private int id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;

    @Builder
    public NoticeResponse(NoticeEntity noticeEntity) {
        this.id = noticeEntity.getId();
        this.title = noticeEntity.getTitle();
        this.content = noticeEntity.getContent();
        this.createdAt = noticeEntity.getCreatedAt();
        this.updatedAt = noticeEntity.getUpdatedAt();
        this.viewCount = noticeEntity.getViewCount();
    }
}
