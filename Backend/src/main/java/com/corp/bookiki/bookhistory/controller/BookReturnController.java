package com.corp.bookiki.bookhistory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookhistory.dto.BookReturnRequest;
import com.corp.bookiki.bookhistory.service.BookReturnService;
import com.corp.bookiki.global.error.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "도서 반납 API", description = "도서 반납 스캔 및 처리 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/books/return")
@RequiredArgsConstructor
public class BookReturnController {

	private final BookReturnService bookReturnService;

	@Operation(
		summary = "도서 반납 스캔 결과 처리",
		description = """
            QR 코드 및 OCR 스캔 결과를 이용하여 도서 반납을 처리합니다.
            책장별로 반납된 도서 목록을 전달하여 올바른 카테고리의 책장에 반납되었는지 검증합니다.
            """
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "반납 처리 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
                    {
                        "scannedBookItemIds": [1, 2, 3],
                        "ocrResults": ["클린 코드", "객체지향의 사실과 오해", "토비의 스프링"],
                        "shelfBookItemsMap": {
                            "1": [1, 2],
                            "2": [3]
                        }
                    }
                    """
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 입력값",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 또는 책장을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@PostMapping("/scan")
	public ResponseEntity<Void> processScanResult(
		@RequestBody BookReturnRequest request
	) {
		bookReturnService.processScanResults(request);

		return ResponseEntity.ok().build();
	}
}