package com.corp.bookiki.bookcheckout.dto;

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
		description = "사용자 ID",
		example = "1",
		required = true
	)
	@NotNull(message = "사용자 ID는 필수 입력값입니다.")
	private Integer userId;

	@Schema(
		description = "도서 아이템 ID",
		example = "1",
		required = true
	)
	@NotNull(message = "도서 아이템 ID는 필수 입력값입니다.")
	private Integer bookItemId;
}
