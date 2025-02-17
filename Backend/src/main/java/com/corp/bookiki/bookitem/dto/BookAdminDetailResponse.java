package com.corp.bookiki.bookitem.dto;

import com.corp.bookiki.bookinformation.entity.Category;
import com.corp.bookiki.bookitem.entity.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@NoArgsConstructor
@Schema(description = "관리자페이지 도서관리 도서 디테일")
@Builder
public class BookAdminDetailResponse {
    // 책 정보 (BookInformationEntity)
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private LocalDateTime publishedAt;
    private String image;
    private String description;
    private Category category;

    // 도서 아이템 정보 (BookItemEntity)
    private Integer id;
    private LocalDateTime purchaseAt;
    private BookStatus bookStatus;

    // QR 코드 정보
    private QrCodeInfo qrCode;

    // 현재 대출자 정보 (대출 중일 때만)
    private BorrowerInfo currentBorrower;

    @Builder
    public BookAdminDetailResponse(String title, String author, String publisher,
                                   String isbn, LocalDateTime publishedAt, String image, String description,
                                   Category category, Integer id, LocalDateTime purchaseAt,
                                   BookStatus bookStatus, QrCodeInfo qrCode, BorrowerInfo currentBorrower) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.publishedAt = publishedAt;
        this.image = image;
        this.description = description;
        this.category = category;
        this.id = id;
        this.purchaseAt = purchaseAt;
        this.bookStatus = bookStatus;
        this.qrCode = qrCode;
        this.currentBorrower = currentBorrower;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QrCodeInfo {
        private Integer id;
        private String qrValue;
        private String createAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BorrowerInfo {
        private Integer userId;
        private String userName;
        private LocalDateTime borrowedAt;
    }
}