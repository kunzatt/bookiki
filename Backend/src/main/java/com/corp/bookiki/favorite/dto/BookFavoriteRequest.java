package com.corp.bookiki.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "도서 좋아요 요청")
public class BookFavoriteRequest {
	@Schema(
		description = "도서 아이템 ID",
		example = "123",
		required = true
	)
	@NotNull(message = "도서 아이템 ID는 필수입니다.")
	@Positive(message = "도서 아이템 ID는 양수여야 합니다.")
	private Integer bookItemId;
}