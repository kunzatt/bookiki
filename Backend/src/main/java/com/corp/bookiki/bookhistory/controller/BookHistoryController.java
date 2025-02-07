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
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.global.error.exception.BookHistoryException;
import com.corp.bookiki.global.error.exception.UserException;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;

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

@Tag(name = "도서 대출 기록 API", description = "도서 대출 기록 조회 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BookHistoryController {
	private final BookHistoryService bookHistoryService;

	@Operation(
		summary = "관리자용 도서 대출 기록 전체 조회",
		description = "관리자가 전체 도서 대출 기록을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Page.class),
				examples = @ExampleObject(
					value = """
                    {
                        "content": [{
                            "id": 1,
                            "userId": 100,
                            "userName": "홍길동",
                            "companyId": "CORP123",
                            "bookItemId": 200,
                            "borrowedAt": "2024-02-04T10:00:00",
                            "returnedAt": "2024-02-18T10:00:00",
                            "isOverdue": false
                        }],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20,
                            "sort": {
                                "sorted": true,
                                "direction": "DESC"
                            }
                        },
                        "totalElements": 1
                    }
                    """
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "권한 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                    {
                        "timestamp": "2024-02-04T10:00:00",
                        "status": 401,
                        "message": "접근 권한이 없습니다",
                        "errors": []
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
		)
	})
	@GetMapping("/admin/book-histories")
	public ResponseEntity<Page<BookHistoryResponse>> getAdminBookHistories(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "조회 기간 타입") @RequestParam PeriodType periodType,
		@Parameter(description = "시작일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
		@Parameter(description = "종료일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
		@Parameter(description = "사용자 이름") @RequestParam(required = false) String userName,
		@Parameter(description = "회사 ID") @RequestParam(required = false) String companyId,
		@Parameter(description = "연체 여부") @RequestParam(required = false) Boolean overdue,
		@Parameter(hidden = true) @PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		if (authUser == null || authUser.getRole() != Role.ADMIN) {
			throw new UserException(ErrorCode.UNAUTHORIZED);
		}

		LocalDate start = getStartDate(periodType, startDate);
		LocalDate end = getEndDate(periodType, endDate);

		return ResponseEntity.ok(
			bookHistoryService.getAdminBookHistories(start, end, userName, companyId, overdue, pageable)
		);
	}

	@Operation(
		summary = "사용자용 도서 대출 기록 조회",
		description = "사용자가 자신의 도서 대출 기록을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Page.class),
				examples = @ExampleObject(
					value = """
                    {
                        "content": [{
                            "id": 1,
                            "userId": 100,
                            "bookItemId": 200,
                            "borrowedAt": "2024-02-04T10:00:00",
                            "returnedAt": "2024-02-18T10:00:00",
                            "isOverdue": false
                        }],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20,
                            "sort": {
                                "sorted": true,
                                "direction": "DESC"
                            }
                        },
                        "totalElements": 1
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
		)
	})
	@GetMapping("/user/book-histories")
	public ResponseEntity<Page<BookHistoryResponse>> getUserBookHistories(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "조회 기간 타입") @RequestParam PeriodType periodType,
		@Parameter(description = "시작일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
		@Parameter(description = "종료일 (YYYY-MM-DD, CUSTOM 타입일 때 필수)")
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
		@Parameter(description = "연체 여부로 필터링") @RequestParam(required = false) Boolean overdue,
		@Parameter(hidden = true) @PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {

		if (authUser == null || authUser.getRole() == null) {
			throw new UserException(ErrorCode.UNAUTHORIZED);
		}

		LocalDate start = getStartDate(periodType, startDate);
		LocalDate end = getEndDate(periodType, endDate);

		return ResponseEntity.ok(
			bookHistoryService.getUserBookHistories(authUser.getId(), start, end, overdue, pageable)
		);
	}

	@Operation(
		summary = "현재 대출 중인 도서 목록 조회",
		description = "사용자의 현재 대출 중인 도서 목록을 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = List.class),
				examples = @ExampleObject(
					value = """
                [{
                    "id": 1,
                    "userId": 100,
                    "bookItemId": 200,
                    "borrowedAt": "2024-02-04T10:00:00",
                    "dueDate": "2024-02-18T10:00:00",
                    "isOverdue": false
                }]
                """
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                {
                    "timestamp": "2024-02-04T10:00:00",
                    "status": 401,
                    "message": "인증되지 않은 사용자입니다",
                    "errors": []
                }
                """
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "대출 기록을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                {
                    "timestamp": "2024-02-04T10:00:00",
                    "status": 404,
                    "message": "대출 기록을 찾을 수 없습니다",
                    "errors": []
                }
                """
				)
			)
		)
	})
	@GetMapping("user/book-histories/current")
	public ResponseEntity<List<BookHistoryResponse>> getCurrentBorrowedBooks(
		@CurrentUser AuthUser authUser,
		@Parameter(description = "연체 도서만 조회") @RequestParam(required = false) Boolean onlyOverdue
	) {

		if (authUser == null || authUser.getRole() == null) {
			throw new UserException(ErrorCode.UNAUTHORIZED);
		}

		return ResponseEntity.ok(
			bookHistoryService.getCurrentBorrowedBooks(authUser.getId(), onlyOverdue)
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