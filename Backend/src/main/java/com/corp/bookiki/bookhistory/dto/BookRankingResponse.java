package com.corp.bookiki.bookhistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
	name = "BookRankingResponse",
	description = "도서 대출 순위 응답"
)
public class BookRankingResponse {

	@Schema(
		description = "도서 아이템 ID",
		example = "100"
	)
	private Integer bookItemId;

	@Schema(
		description = "도서 제목",
		example = "스프링 부트와 AWS로 혼자 구현하는 웹 서비스"
	)
	private String title;

	@Schema(
		description = "도서 저자",
		example = "이동욱"
	)
	private String author;

	@Schema(
		description = "도서 카테고리 코드",
		example = "1"
	)
	private Integer category;

	@Schema(
		description = "도서 이미지 URL",
		example = "https://example.com/book-cover.jpg"
	)
	private String image;

	@Schema(
		description = "도서 대출 횟수",
		example = "42"
	)
	private Integer borrowCount;

}