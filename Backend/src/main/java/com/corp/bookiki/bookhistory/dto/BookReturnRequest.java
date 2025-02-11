package com.corp.bookiki.bookhistory.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "도서 반납 요청 DTO")
public class BookReturnRequest {
	@Schema(
		description = "OCR로 인식된 도서 제목 목록",
		example = "[\"클린 코드\", \"객체지향의 사실과 오해\", \"토비의 스프링\"]"
	)
	private List<String> ocrResults;

	@Schema(
		description = "책장별 도서 ID 매핑 (key: 책장ID, value: 해당 착장에 있는 도서ID 목록)",
		example = "{\"1\": [1, 2], \"2\": [3]}"
	)
	private Map<Integer, List<Integer>> shelfBookItemsMap;
}
