package com.corp.bookiki.bookitem.controller;

import com.corp.bookiki.bookitem.dto.*;
import com.corp.bookiki.bookitem.enums.SearchType;
import com.corp.bookiki.bookitem.service.BookItemService;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "도서 아이템 API", description = "도서 아이템 조회 API")
@Slf4j
public class BookItemController {

	private final BookItemService bookItemService;

	@Operation(summary = "도서 아이템 목록 검색", description = "검색 타입과 키워드로 도서 아이템 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
				responseCode = "200",
				description = "도서 아이템 목록 검색 조회 성공",
				content = @Content(
						mediaType = "application/json",
						schema = @Schema(implementation = BookItemListResponse.class)
				)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 아이템 조회 결과가 없습니다.",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
				)
		)
	})
	@GetMapping("/books/search")
	public ResponseEntity<?> selectBooks(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam SearchType type,
			@RequestParam(required = false) String keyword
	) {
		Page<BookItemListResponse> books = bookItemService.selectBooks(type, keyword, page, size);
		return ResponseEntity.ok(books);
	}

	@Operation(summary = "도서 아이템 목록 조회", description = "페이지네이션과 정렬 조건을 통해 도서 아이템 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 아이템 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookItemResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 아이템 조회 결과가 없습니다.",
			content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping("/books/search/list")
	public Page<BookItemDisplayResponse> selectBooksByKeyword(
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
		@RequestParam(defaultValue = "0") int page,

		@Parameter(description = "페이지당 항목 수", example = "10")
		@RequestParam(defaultValue = "10") int size,

		@Parameter(description = "정렬 기준 필드", example = "id")
		@RequestParam(defaultValue = "id") String sortBy,

		@Parameter(description = "정렬 방향 (asc/desc)", example = "desc")
		@RequestParam(defaultValue = "desc") String direction,

		@Parameter(description = "검색 키워드", example = "test")
		@RequestParam(required = false) String keyword
	) {
		log.info("도서 아이템 목록 조회: page={}, size={}, sortBy={}, direction={}, keyword={}", page, size, sortBy, direction, keyword);
		return bookItemService.selectBooksByKeyword(page, size, sortBy, direction, keyword);
	}

	@Operation(summary = "보유 도서 아이템 ID로 조회", description = "ID를 통해 특정 도서 아이템을 조회합니다.")
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
	@GetMapping("/books/search/qrcodes/{id}")
	public BookItemResponse getBookItemById(
		@Parameter(description = "조회할 도서 아이템의 ID", required = true, example = "1")
		@PathVariable Integer id
	) {
		log.info("도서 아이템 단건 조회: id={}", id);
		return bookItemService.getBookItemById(id);
	}

	@Operation(summary = "도서 아이템 삭제", description = "ID를 통해 특정 도서 아이템을 삭제(soft delete)합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 아이템 삭제 성공",
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
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 삭제된 도서 아이템",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@DeleteMapping("/books/search/{id}")
	public ResponseEntity<BookItemResponse> deleteBookItem(
		@Parameter(description = "삭제할 도서 아이템의 ID", required = true, example = "1")
		@PathVariable Integer id
	) {
		log.info("도서 아이템 삭제: id={}", id);
		BookItemResponse response = bookItemService.deleteBookItem(id);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "도서 아이템 등록", description = "새로운 도서 아이템을 등록합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "도서 아이템 등록 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookItemResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "도서 정보를 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@PostMapping("/admin/books/search")
	public ResponseEntity<BookItemResponse> addBookItem(
		@Parameter(description = "도서 구매 정보", required = true)
		@RequestBody BookItemRequest bookItemRequest
	) {
		log.info("도서 아이템 등록: bookInformationId={}, purchaseAt={}",
			bookItemRequest.getBookInformationId(), bookItemRequest.getPurchaseAt());

		bookItemRequest = BookItemRequest.builder()
			.bookInformationId(bookItemRequest.getBookInformationId())
			.purchaseAt(bookItemRequest.getPurchaseAt())
			.build();

		BookItemResponse response = bookItemService.addBookItem(bookItemRequest);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "관리자 - 도서 아이템 목록 조회", description = "관리자 페이지에서 도서 아이템 목록을 페이지 단위로 조회합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "도서 아이템 목록 조회 성공",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(
									implementation = Page.class,
									subTypes = {BookAdminListResponse.class}
							)
					)
			),
			@ApiResponse(
					responseCode = "400",
					description = "잘못된 요청 파라미터",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class)
					)
			)
	})

	@GetMapping("/admin/bookManage")
	public ResponseEntity<?> selectAllBooksForAdmin(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword
	) {
		log.info("Admin 도서 아이템 조회 시작");
		Page<BookAdminListResponse> books = bookItemService.selectBooksForAdmin(keyword, page, size);
		return ResponseEntity.ok(books);
	}

	@PatchMapping("/admin/bookManage/{id}/status")
	@Operation(summary = "도서 상태 수정", description = "도서의 상태를 변경합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "도서 상태 수정 성공"
			),
			@ApiResponse(
					responseCode = "404",
					description = "도서를 찾을 수 없음",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class)
					)
			)
	})
	public ResponseEntity<Void> updateBookStatus(
			@PathVariable Integer id,
			@RequestBody @Valid UpdateBookStatusRequest request
	) {
		bookItemService.updateBookStatus(id, request.getStatus());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/admin/books/{bookItemId}")
	public ResponseEntity<BookAdminDetailResponse> getBookDetail(@PathVariable Integer bookItemId) {
		return ResponseEntity.ok(bookItemService.getBookAdminDetail(bookItemId));
	}
}