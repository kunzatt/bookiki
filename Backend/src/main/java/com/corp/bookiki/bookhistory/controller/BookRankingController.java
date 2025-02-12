package com.corp.bookiki.bookhistory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookhistory.dto.BookRankingResponse;
import com.corp.bookiki.bookhistory.service.BookRankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "도서 랭킹 API", description = "도서 대출 순위 관련 API")
@RestController
@RequestMapping("/api/books/ranking")
@RequiredArgsConstructor
@Slf4j
public class BookRankingController {

	private final BookRankingService bookRankingService;

	@Operation(
		summary = "도서 대출 순위 조회",
		description = "최근 한달간 가장 많이 대출된 도서 TOP 10을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "랭킹 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookRankingResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "랭킹 데이터가 존재하지 않음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping
	public ResponseEntity<List<BookRankingResponse>> getBookRanking() {
		return ResponseEntity.ok(bookRankingService.getBookRanking());
	}
}
