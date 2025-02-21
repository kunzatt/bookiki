package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookitem.entity.BookStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateBookStatusRequest {

    private BookStatus status;
}
