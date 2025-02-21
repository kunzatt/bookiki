package com.corp.bookiki.notice.dto;

import com.corp.bookiki.notice.entity.NoticeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "공지사항 수정")
public class NoticeUpdate {
    @Schema(description = "공지사항 ID", example = "1", required = true)
    private Integer id;

    @Schema(description = "제목", example = "시스템 점검 안내", required = true)
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @Schema(description = "내용", example = "2024년 1월 25일 오전 2시부터 4시까지 시스템 점검이 진행됩니다.")
    private String content;

    // Entity 변환 메서드
    public NoticeEntity toEntity() {
        return NoticeEntity.builder()
                .title(this.title)
                .content(this.content)
                .build();
    }
}
