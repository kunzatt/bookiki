package com.corp.bookiki.bookcheckout.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.bookcheckout.enitity.BookHistoryEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "도서 대출 응답")
public class BookBorrowResponse {
	@Schema(
		description = "대출 기록 ID",
		example = "1"
	)
	private Integer id;

	@Schema(
		description = "도서 아이템 ID",
		example = "1"
	)
	private Integer bookItemId;

	@Schema(
		description = "사용자 ID",
		example = "1"
	)
	private Integer userId;

	@Schema(
		description = "대출 일시",
		example = "2024-02-04T14:30:00"
	)
	private LocalDateTime borrowedAt;

	public static BookBorrowResponse from(BookHistoryEntity history) {
		return BookBorrowResponse.builder()
			.id(history.getId())
			.bookItemId(history.getBookItem().getId())
			.userId(history.getUser().getId())
			.borrowedAt(history.getBorrowedAt())
			.build();
	}
}