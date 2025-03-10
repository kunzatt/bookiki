package com.corp.bookiki.bookhistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "도서 대출 요청")
public class BookBorrowRequest {
	@Schema(
		description = "도서 아이템 ID",
		example = "1",
		required = true
	)
	@NotNull(message = "도서 아이템 ID는 필수 입력값입니다.")
	private Integer bookItemId;
}
