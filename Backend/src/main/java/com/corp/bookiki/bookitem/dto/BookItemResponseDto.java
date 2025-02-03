package com.corp.bookiki.bookitem.dto;

import java.time.LocalDateTime;

import com.corp.bookiki.bookitem.entity.BookItemEntity;
import com.corp.bookiki.bookitem.entity.StatusType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookItemResponseDto {
	private Integer id;
	private Integer bookInformationId;
	private LocalDateTime purchaseAt;
	private StatusType statusType;
	private LocalDateTime updatedAt;
	private QRCodeInfo qrCode;

	@Getter
	@Setter
	public static class QRCodeInfo {
		private Integer id;
		private String qrValue;
		private LocalDateTime createdAt;
	}

	public BookItemResponseDto(BookItemEntity bookItem) {
		this.id = bookItem.getId();

		if (bookItem.getBookInformation() != null) {
			this.bookInformationId = bookItem.getBookInformation().getId();
		}

		this.purchaseAt = bookItem.getPurchaseAt();
		this.statusType = bookItem.getStatusType();
		this.updatedAt = bookItem.getUpdatedAt();

		if (bookItem.getQrCode() != null) {
			this.qrCode = new QRCodeInfo();
			this.qrCode.setId(bookItem.getQrCode().getId());
			this.qrCode.setQrValue(bookItem.getQrCode().getQrValue());
			this.qrCode.setCreatedAt(bookItem.getQrCode().getCreatedAt());
		}
	}
}
