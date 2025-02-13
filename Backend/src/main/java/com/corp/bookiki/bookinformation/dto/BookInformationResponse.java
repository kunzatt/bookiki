package com.corp.bookiki.bookinformation.dto;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;

import com.corp.bookiki.bookinformation.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "책 정보 응답")
public class BookInformationResponse {

	@Schema(description = "도서 ID", example = "1")
	private Integer id;

	@Schema(
		description = "제목",
		example = "클린 코드",
		required = true
	)
	@NotBlank(message = "제목은 필수 입력값입니다.")
	private String title;

	@Schema(
		description = "저자",
		example = "로버트 C. 마틴",
		required = true
	)
	@NotBlank(message = "저자는 필수 입력값입니다.")
	private String author;

	@Schema(
		description = "출판사",
		example = "인사이트"
	)
	private String publisher;

	@Schema(
		description = "ISBN",
		example = "9788966260959",
		required = true
	)
	@NotBlank(message = "ISBN은 필수 입력값입니다.")
	private String isbn;

	@Schema(description = "출판일")
	private String publishedAt;

	@Schema(description = "책 표지 이미지 URL")
	private String image;

	@Schema(description = "책 설명")
	private String description;

	@Schema(description = "카테고리")
	private Category category;

	@Schema(name = "BookInformationResponse", description = "책 정보 응답")
	public static BookInformationResponse from(
		@Schema(description = "책 정보 엔티티")
		BookInformationEntity entity
	) {
		BookInformationResponse response = new BookInformationResponse();
		response.setId(entity.getId());
		response.setTitle(entity.getTitle());
		response.setAuthor(entity.getAuthor());
		response.setPublisher(entity.getPublisher());
		response.setIsbn(entity.getIsbn());
		response.setPublishedAt(entity.getPublishedAt().toString());
		response.setImage(entity.getImage());
		response.setDescription(entity.getDescription());
		response.setCategory(Category.ofCode(entity.getCategory()));
		return response;
	}
}