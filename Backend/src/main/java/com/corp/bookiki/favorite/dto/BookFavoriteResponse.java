package com.corp.bookiki.favorite.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.favorite.entity.FavoriteEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도서 좋아요 응답")
public class BookFavoriteResponse {
	@Schema(description = "좋아요 ID", example = "1")
	private Integer id;

	@Schema(description = "도서 아이템 ID", example = "4")
	private Integer bookItemId;

	@Schema(description = "요청한 유저 ID", example = "2")
	private Integer userId;

	@Schema(description = "도서 제목", example = "초보자를 위한 Java 200제")
	private String bookTitle;

	@Schema(description = "도서 이미지 URL", example = "https://shopping-phinf.pstatic.net/main_3246371/32463719072.20221019135443.jpg")
	private String bookImage;

	@Schema(description = "좋아요 생성 시간")
	private LocalDateTime createdAt;

	public static BookFavoriteResponse from(FavoriteEntity entity) {
		return BookFavoriteResponse.builder()
			.id(entity.getId())
			.bookItemId(entity.getBookItem().getId())
			.userId(entity.getUser().getId())
			.bookTitle(entity.getBookItem().getBookInformation().getTitle())
			.bookImage(entity.getBookItem().getBookInformation().getImage())
			.createdAt(entity.getCreatedAt())
			.build();
	}
}