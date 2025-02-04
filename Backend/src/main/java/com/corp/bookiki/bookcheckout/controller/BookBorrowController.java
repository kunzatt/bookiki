package com.corp.bookiki.bookcheckout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookcheckout.dto.BookBorrowRequest;
import com.corp.bookiki.bookcheckout.dto.BookBorrowResponse;
import com.corp.bookiki.bookcheckout.service.BookBorrowService;
import com.corp.bookiki.global.error.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "도서 대출 API", description = "도서 대출 관련 API")
@Slf4j
public class BookBorrowController {

	private final BookBorrowService bookBorrowService;

	@PostMapping("/borrow")
	@Operation(summary = "도서 대출", description = "사용자가 도서를 대출합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "대출 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookBorrowResponse.class),
				examples = @ExampleObject(
					value = """
                            {
                                "borrowId": 1,
                                "userId": 100,
                                "bookItemId": 200,
                                "borrowDate": "2024-02-04T10:00:00",
                                "dueDate": "2024-02-18T10:00:00"
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
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                            {
                                "timestamp": "2024-02-04T10:00:00",
                                "status": 400,
                                "message": "잘못된 입력값입니다",
                                "errors": []
                            }
                            """
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "존재하지 않는 도서",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                            {
                                "timestamp": "2024-02-04T10:00:00",
                                "status": 404,
                                "message": "존재하지 않는 도서입니다",
                                "errors": []
                            }
                            """
				)
			)
		)
	})
	public ResponseEntity<BookBorrowResponse> borrowBook(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "도서 대출 정보",
			required = true,
			content = @Content(schema = @Schema(implementation = BookBorrowRequest.class))
		)
		@Valid @RequestBody BookBorrowRequest bookBorrowRequest
	) {
		log.info("도서 대출 요청: userId={}, bookItemId={}",
			bookBorrowRequest.getUserId(),
			bookBorrowRequest.getBookItemId());

		return ResponseEntity.ok(bookBorrowService.borrowBook(bookBorrowRequest));
	}
}