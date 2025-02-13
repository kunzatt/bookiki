package com.corp.bookiki.favorite.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.corp.bookiki.favorite.dto.BookFavoriteResponse;
import com.corp.bookiki.favorite.service.BookFavoriteService;
import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.AuthUser;

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
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "도서 좋아요 API", description = "도서 좋아요 관련 API")
@Slf4j
public class BookFavoriteController {

	private final BookFavoriteService bookFavoriteService;

	@GetMapping("/{bookItemId}")
	@Operation(summary = "도서 좋아요 여부 확인", description = "로그인한 사용자가 해당 도서에 좋아요를 했는지 확인합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "좋아요 여부 확인 성공"
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<Boolean> checkFavorite(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "확인할 도서의 ID", required = true, example = "1")
		@PathVariable Integer bookItemId) {

		log.info("도서 좋아요 여부 확인: userId={}, bookItemId={}", authUser.getId(), bookItemId);
		return ResponseEntity.ok(bookFavoriteService.checkFavorite(authUser.getId(), bookItemId));
	}

	@GetMapping()
	@Operation(summary = "내 좋아요 목록 조회", description = "로그인한 사용자의 전체 좋아요 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "좋아요 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookFavoriteResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	public ResponseEntity<Page<BookFavoriteResponse>> getUserFavorites(
		@CurrentUser AuthUser authUser,
		Pageable pageable) {

		log.info("내 좋아요 목록 조회: userId={}", authUser.getId());
		return ResponseEntity.ok(bookFavoriteService.getUserFavorites(authUser.getId(), pageable));
	}

	@PostMapping("/{bookItemId}")
	@Operation(summary = "도서 좋아요 토글", description = "도서 좋아요를 추가하거나 취소합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "좋아요 토글 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookFavoriteResponse.class)
			)
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
	public ResponseEntity<String> toggleFavorite(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "도서 ID", required = true, example = "1")
		@PathVariable Integer bookItemId) {

		log.info("도서 좋아요 토글: userId={}, bookItemId={}", authUser.getId(), bookItemId);
		String success = bookFavoriteService.toggleFavorite(authUser.getId(), bookItemId);
		return ResponseEntity.ok().body(success + " 성공");
	}

	@GetMapping("/count/{bookItemId}")
	@Operation(summary = "도서의 좋아요 수 조회", description = "특정 도서의 전체 좋아요 수를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "좋아요 수 조회 성공"
		)
	})
	public ResponseEntity<Integer> getBookFavoriteCount(
		@Parameter(description = "조회할 도서의 ID", required = true, example = "1")
		@PathVariable Integer bookItemId) {

		log.info("도서의 좋아요 수 조회: bookItemId={}", bookItemId);
		return ResponseEntity.ok(bookFavoriteService.getBookFavoriteCount(bookItemId));
	}

	@GetMapping("/count")
	@Operation(summary = "내 좋아요 수 조회", description = "로그인한 사용자의 전체 좋아요 수를 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "좋아요 수 조회 성공"
		)
	})
	public ResponseEntity<Integer> getUserFavoriteCount(
		@CurrentUser AuthUser authUser) {

		log.info("내 좋아요 수 조회: userId={}", authUser.getId());
		return ResponseEntity.ok(bookFavoriteService.getUserFavoriteCount(authUser.getId()));
	}
}