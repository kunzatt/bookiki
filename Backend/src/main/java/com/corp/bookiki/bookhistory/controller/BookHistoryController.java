package com.corp.bookiki.bookhistory.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.bookhistory.service.BookHistoryService;
import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "도서 대출 기록 API", description = "도서 대출 기록 조회 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BookHistoryController {
	private final BookHistoryService bookHistoryService;

	@Operation(summary = "관리자용 도서 대출 기록 전체 조회")
	@GetMapping("/admin/book-histories")
	public ResponseEntity<Page<BookHistoryResponse>> getAdminBookHistories(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "조회 기간 타입")
		@RequestParam PeriodType periodType,

		@Parameter(description = "시작일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

		@Parameter(description = "종료일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,

		@Parameter(description = "사용자 이름")
		@RequestParam(required = false) String userName,

		@Parameter(description = "회사 ID")
		@RequestParam(required = false) String companyId,

		@Parameter(description = "연체 여부")
		@RequestParam(required = false) Boolean overdue,

		@Parameter(hidden = true)
		@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {

		if (authUser.getRole() != Role.ADMIN) {
			throw new UserException(ErrorCode.UNAUTHORIZED);
		}

		LocalDate start = getStartDate(periodType, startDate);
		LocalDate end = getEndDate(periodType, endDate);

		return ResponseEntity.ok(
			bookHistoryService.getAdminBookHistories(start, end, userName, companyId, overdue, pageable)
		);
	}

	@Operation(summary = "사용자용 도서 대출 기록 조회")
	@GetMapping("/user/book-histories")
	public ResponseEntity<Page<BookHistoryResponse>> getUserBookHistories(
		@CurrentUser AuthUser authUser,

		@Parameter(description = "조회 기간 타입")
		@RequestParam PeriodType periodType,

		@Parameter(description = "시작일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

		@Parameter(description = "종료일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,

		@Parameter(description = "연체 여부로 필터링")
		@RequestParam(required = false) Boolean overdue,

		@Parameter(hidden = true)
		@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		LocalDate start = getStartDate(periodType, startDate);
		LocalDate end = getEndDate(periodType, endDate);

		return ResponseEntity.ok(
			bookHistoryService.getUserBookHistories(authUser, start, end, overdue, pageable)
		);
	}

	@Operation(summary = "현재 대출 중인 도서 목록 조회")
	@GetMapping("user/book-histories/current")
	public ResponseEntity<List<BookHistoryResponse>> getCurrentBorrowedBooks(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "연체 도서만 조회")
		@RequestParam(required = false) Boolean onlyOverdue
	) {
		return ResponseEntity.ok(
			bookHistoryService.getCurrentBorrowedBooks(authUser, onlyOverdue)
		);
	}

	private LocalDate getStartDate(PeriodType periodType, LocalDate startDate) {
		if (periodType == PeriodType.CUSTOM) {
			if (startDate == null) {
				throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE_NO_DATE);
			}
			return startDate;
		}
		return periodType.getStartDate();
	}

	private LocalDate getEndDate(PeriodType periodType, LocalDate endDate) {
		if (periodType == PeriodType.CUSTOM) {
			if (endDate == null) {
				throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE_NO_DATE);
			}
			if (endDate.isBefore(getStartDate(periodType, endDate))) {
				throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE_AFTER_DATE);
			}
			return endDate;
		}
		return LocalDate.now();
	}
}