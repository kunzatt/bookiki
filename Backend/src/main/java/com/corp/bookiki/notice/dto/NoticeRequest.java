package com.corp.bookiki.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeRequest {
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "내용을 필수 입력값입니다.")
    private String content;
}
