package com.corp.bookiki.bookitem.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.StatusType;
import com.corp.bookiki.qrcode.dto.QrCodeResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "도서 아이템 응답 정보")
public class BookItemResponse {
	@Schema(
		description = "도서 아이템 고유 식별자",
		example = "1",
		required = true
	)
	private Integer id;

	@Schema(
		description = "도서 정보 고유 식별자",
		example = "1",
		required = true
	)
	private Integer bookInformationId;

	@Schema(
		description = "도서 구매 일시",
		example = "2024-02-03T10:30:00",
		required = true
	)
	private LocalDateTime purchaseAt;

	@Schema(
		description = "도서 상태",
		example = "AVAILABLE",
		required = true
	)
	private StatusType statusType;

	@Schema(
		description = "도서 정보 수정 일시",
		example = "2024-02-03T10:30:00",
		required = true
	)
	private LocalDateTime updatedAt;

	@Schema(
		description = "QR 코드 정보",
		required = false
	)
	private QrCodeResponse qrCode;

	public BookItemResponse(BookItemEntity bookItem) {
		this.id = bookItem.getId();

		if (bookItem.getBookInformation() != null) {
			this.bookInformationId = bookItem.getBookInformation().getId();
		}

		this.purchaseAt = bookItem.getPurchaseAt();
		this.statusType = bookItem.getStatusType();
		this.updatedAt = bookItem.getUpdatedAt();

		if (bookItem.getQrCode() != null) {
			this.qrCode = new QrCodeResponse(bookItem.getQrCode());
		}
	}
}