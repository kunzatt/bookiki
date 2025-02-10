package com.corp.bookiki.bookhistory.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "도서 반납 스캔 요청")
public class BookReturnRequest {

	@Schema(
		description = "스캔된 도서 아이템 ID 목록",
		example = "[1, 2, 3]",
		required = true
	)
	private List<Integer> scannedBookItemIds;

	@Schema(
		description = "OCR 스캔 결과 목록",
		example = "[\"Book1\", \"Book2\", \"Book3\"]",
		required = true
	)
	private List<String> ocrResults;
}