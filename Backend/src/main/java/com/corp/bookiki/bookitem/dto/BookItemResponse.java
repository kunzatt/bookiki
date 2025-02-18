package com.corp.bookiki.bookitem.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.BookStatus;
import com.corp.bookiki.qrcode.dto.QrCodeResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "도서 아이템 응답 정보")
public class BookItemResponse {
	@Schema(description = "도서 아이템 고유 식별자", example = "1")
	private Integer id;

	@Schema(description = "도서 정보 고유 식별자", example = "1")
	private Integer bookInformationId;

	@Schema(description = "도서 구매 일시", type = "string", format = "date-time")
	private LocalDateTime purchaseAt;

	@Schema(
		description = "도서 상태 (AVAILABLE: 대출 가능, UNAVAILABLE: 대출 불가, LOST: 분실, DAMAGED: 파손)",
		example = "AVAILABLE"
	)
	private BookStatus bookStatus;

	@Schema(description = "도서 정보 수정 일시", type = "string", format = "date-time")
	private LocalDateTime updatedAt;

	@Schema(description = "QR 코드 정보")
	private QrCodeResponse qrCode;

	@Builder
	public BookItemResponse(Integer id, Integer bookInformationId, LocalDateTime purchaseAt,
		BookStatus bookStatus, LocalDateTime updatedAt, QrCodeResponse qrCode) {
		this.id = id;
		this.bookInformationId = bookInformationId;
		this.purchaseAt = purchaseAt;
		this.bookStatus = bookStatus;
		this.updatedAt = updatedAt;
		this.qrCode = qrCode;
	}

	public static BookItemResponse from(BookItemEntity bookItem) {
		return BookItemResponse.builder()
			.id(bookItem.getId())
			.bookInformationId(bookItem.getBookInformation() != null ?
				bookItem.getBookInformation().getId() : null)
			.purchaseAt(bookItem.getPurchaseAt())
			.bookStatus(bookItem.getBookStatus())
			.updatedAt(bookItem.getUpdatedAt())
			.qrCode(bookItem.getQrCode() != null ?
				new QrCodeResponse(bookItem.getQrCode()) : null)
			.build();
	}
}