package com.corp.bookiki.bookhistory.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.bookhistory.enitity.BookHistoryEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
	name = "BookHistoryResponse",
	description = "도서 대출 기록 응답"
)
public class BookHistoryResponse {
	@Schema(
		description = "대출 기록 ID",
		example = "1"
	)
	private Integer id;

	@Schema(
		description = "도서 아이템 ID",
		example = "100"
	)
	private Integer bookItemId;

	@Schema(
		description = "사용자 ID",
		example = "200"
	)
	private Integer userId;

	@Schema(
		description = "대출 일시",
		type = "string",
		format = "date-time",
		example = "2024-02-04T14:30:00"
	)
	private LocalDateTime borrowedAt;

	@Schema(
		description = "반납 일시",
		type = "string",
		format = "date-time",
		example = "2024-02-18T14:30:00"
	)
	private LocalDateTime returnedAt;

	@Schema(
		description = "도서 제목",
		example = "스프링 부트와 AWS로 혼자 구현하는 웹 서비스"
	)
	private String bookTitle;

	@Schema(
		description = "도서 저자",
		example = "이동욱"
	)
	private String bookAuthor;

	public static BookHistoryResponse from(BookHistoryEntity entity) {
		return BookHistoryResponse.builder()
			.id(entity.getId())
			.bookItemId(entity.getBookItem().getId())
			.userId(entity.getUser().getId())
			.borrowedAt(entity.getBorrowedAt())
			.returnedAt(entity.getReturnedAt())
			.bookTitle(entity.getBookItem().getBookInformation().getTitle())
			.bookAuthor(entity.getBookItem().getBookInformation().getAuthor())
			.build();
	}
}