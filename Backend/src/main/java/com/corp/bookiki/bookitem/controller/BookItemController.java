package com.corp.bookiki.bookitem.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookitem.dto.BookItemResponse;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.error.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books/search")
@RequiredArgsConstructor
@Tag(name = "도서 아이템 API", description = "도서 아이템 조회 API")
@Slf4j
public class BookItemController {

	private final BookItemService bookItemService;

	@Operation(summary = "도서 아이템 목록 조회", description = "페이지네이션과 정렬 조건을 통해 도서 아이템 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 아이템 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookItemResponse.class)
			)
		)
	})
	@GetMapping("/list")
	public Page<BookItemResponse> getAllBookItems(
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") int page,

		@Parameter(description = "페이지당 항목 수", example = "10")
		@RequestParam(defaultValue = "10") int size,

		@Parameter(description = "정렬 기준 필드", example = "id")
		@RequestParam(defaultValue = "id") String sortBy,

		@Parameter(description = "정렬 방향 (asc/desc)", example = "desc")
		@RequestParam(defaultValue = "desc") String direction
	) {
		log.info("도서 아이템 목록 조회: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
		return bookItemService.getAllBookItems(page, size, sortBy, direction);
	}

	@Operation(summary = "도서 아이템 단건 조회", description = "ID를 통해 특정 도서 아이템을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 아이템 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookItemResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 아이템을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping("/qrcodes/{id}")
	public BookItemResponse getBookItemById(
		@Parameter(description = "조회할 도서 아이템의 ID", required = true, example = "1")
		@PathVariable Integer id
	) {
		log.info("도서 아이템 단건 조회: id={}", id);
		return bookItemService.getBookItemById(id);
	}
}