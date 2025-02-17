package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookinformation.entity.BookInformationEntity;
import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.qrcode.dto.QrCodeResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@Getter
@NoArgsConstructor
@Schema(description = "관리자페이지 도서관리 도서 리스트")
public class BookAdminListResponse {
    @Schema(description = "도서 아이템 고유 식별자", example = "1")
    private Integer id;

    @Schema(description = "제목", example = "클린 코드")
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @Schema(description = "isbn", example = "9788960777330")
    @NotBlank(message = "isbn은 필수 입력값입니다.")
    private String isbn;

    @Schema(description = "카테고리", example = "0")
    private Category category;

    @Schema(description = "도서 상태", example = "AVAILABLE")
    private BookStatus bookStatus;

    @Schema(description = "QR 코드 정보")
    private Integer qrCode;

    public BookAdminListResponse(BookItemEntity bookItem) {
        try {
            this.id = bookItem.getId();
            this.bookStatus = bookItem.getBookStatus();
            this.qrCode = bookItem.getQrCode() != null ? bookItem.getQrCode().getId() : null;

            BookInformationEntity bookInfo = bookItem.getBookInformation();
            if (bookInfo != null) {
                this.title = bookInfo.getTitle();
                this.isbn = bookInfo.getIsbn();
                this.category = Category.ofCode(bookInfo.getCategory());
            }
        } catch (Exception e) {
            log.error("DTO 변환 중 에러 발생: {}", e.getMessage());
            throw e;
        }
    }
}