package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "도서 아이템 응답 정보(가상 책장)")
public class BookItemDisplayResponse {
    @Schema(description = "도서 ID", example = "1")
    private Integer id;

    @Schema(description = "책 표지 이미지 URL")
    private String image;

    public static BookItemDisplayResponse from(BookItemEntity bookItemEntity) {
        BookItemDisplayResponse response = new BookItemDisplayResponse();
        response.id = bookItemEntity.getId();
        response.image = bookItemEntity.getBookInformation().getImage();
        return response;
    }
}
