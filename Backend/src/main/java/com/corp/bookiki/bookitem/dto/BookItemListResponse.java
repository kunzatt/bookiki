package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "도서 아이템 응답 정보(가상 책장)")
public class BookItemListResponse {

    @Schema(description = "도서 ID", example = "1")
    private Integer id;

    @Schema(description = "책 표지 이미지 URL")
    private String image;

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

    public static BookItemListResponse from(BookItemEntity bookItemEntity) {
        BookInformationEntity bookinfo = bookItemEntity.getBookInformation();
        BookItemListResponse response = new BookItemListResponse();
        response.id = bookItemEntity.getId();
        response.image = bookinfo.getImage();
        response.title = bookinfo.getTitle();
        response.author = bookinfo.getAuthor();
        return response;
    }

}
