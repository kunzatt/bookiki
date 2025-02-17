package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookitem.entity.BookStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateBookStatusRequest {
    @NotBlank(message = "상태는 필수 입력값입니다.")
    private BookStatus status;
}
