package com.corp.bookiki.bookinformation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookinformation.dto.BookInformationResponse;
import com.corp.bookiki.bookinformation.service.BookInformationService;
import com.corp.bookiki.global.error.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "도서 정보 API", description = "도서 정보 조회 API")
@Slf4j
public class BookInformationController {

	private final BookInformationService bookInformationService;

	@GetMapping("/info/isbn/{isbn}")
	@Operation(summary = "ISBN으로 도서 정보 조회", description = "제공된 ISBN을 통해 도서 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 정보 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookInformationResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 정보를 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
						{
						    "timestamp": "2024-01-23T10:00:00",
						    "status": 404,
						    "message": "도서 정보를 찾을 수 없습니다"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 ISBN",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
						{
						    "timestamp": "2024-01-23T10:00:00",
						    "status": 400,
						    "message": "잘못된 ISBN 입니다"
						}
						"""
				)
			)
		)
	})
	public ResponseEntity<BookInformationResponse> getBookInformationByIsbn(
		@Parameter(description = "조회할 도서의 ISBN", required = true, example = "9788937460470")
		@PathVariable(name = "isbn") String isbn) {

		log.info("ISBN으로 도서 정보 조회: {}", isbn);
		BookInformationResponse bookInfo = bookInformationService.addBookInformationByIsbn(isbn);
		return ResponseEntity.ok(bookInfo);
	}

	@GetMapping("/info/{bookInformationId}")
	@Operation(summary = "id로 도서 정보 조회", description = "제공된 id를 통해 도서 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 정보 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookInformationResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 정보를 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
						{
						    "timestamp": "2024-01-23T10:00:00",
						    "status": 404,
						    "message": "도서 정보를 찾을 수 없습니다"
						}
						"""
				)
			)
		),
	})
	public ResponseEntity<BookInformationResponse> getBookInformation(
		@Parameter(description = "조회할 도서의 id", required = true, example = "3")
		@PathVariable(name = "bookInformationId") int bookInformationId) {

		log.info("id로 도서 정보 조회: {}", bookInformationId);
		BookInformationResponse bookInfo = bookInformationService.getBookInformation(bookInformationId);
		return ResponseEntity.ok(bookInfo);
	}
}