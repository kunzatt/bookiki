package com.corp.bookiki.bookitem.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookItemRequest {
	private Integer bookInformationId;
	private LocalDateTime purchaseAt;

	@Builder
	public BookItemRequest(Integer bookInformationId, LocalDateTime purchaseAt) {
		this.bookInformationId = bookInformationId;
		this.purchaseAt = purchaseAt;
	}
}
