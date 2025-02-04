package com.corp.bookiki.bookhistory.controller;

import java.time.LocalDate;

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
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "도서 대출 기록 API", description = "도서 대출 기록 조회 관련 API")
@RestController
@RequestMapping("/api/book-histories")
@RequiredArgsConstructor
@Slf4j
public class BookHistoryController {
	private final BookHistoryService bookHistoryService;

	@Operation(
		summary = "도서 대출 기록 조회",
		description = """
            기간별 도서 대출 기록을 조회합니다.
            
            - periodType이 CUSTOM인 경우 startDate, endDate가 필수입니다.
            - 다른 periodType인 경우 startDate, endDate는 자동으로 설정됩니다.
            
            조회 가능한 기간 타입:
            - LAST_WEEK: 최근 1주일
            - LAST_MONTH: 최근 1개월
            - LAST_THREE_MONTHS: 최근 3개월
            - LAST_SIX_MONTHS: 최근 6개월
            - LAST_YEAR: 최근 1년
            - CUSTOM: 사용자 지정 기간
            
            검색 기능:
            - keyword 파라미터로 도서 제목 또는 저자 검색 가능
            """
	)
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공",
		content = @Content(
			mediaType = "application/json",
			array = @ArraySchema(schema = @Schema(implementation = BookHistoryResponse.class))
		)
	)
	@ApiResponse(
		responseCode = "400",
		description = """
            잘못된 요청
            - 필수 파라미터 누락
            - CUSTOM 타입일 때 시작일/종료일 누락
            - 시작일이 종료일보다 늦은 경우
            """
	)
	@ApiResponse(
		responseCode = "404",
		description = "대출 기록을 찾을 수 없음"
	)
	@GetMapping
	public ResponseEntity<Page<BookHistoryResponse>> getBookHistories(
		@Parameter(
			description = "조회 기간 타입",
			required = true,
			schema = @Schema(implementation = PeriodType.class)
		)
		@RequestParam PeriodType periodType,

		@Parameter(
			description = "시작일 (YYYY-MM-DD)",
			example = "2024-02-04"
		)
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

		@Parameter(
			description = "종료일 (YYYY-MM-DD)",
			example = "2024-02-18"
		)
		@RequestParam(required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,

		@Parameter(
			description = "검색 키워드 (도서 제목 또는 저자)",
			example = "스프링"
		)
		@RequestParam(required = false) String keyword,

		@Parameter(hidden = true)
		@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		log.info("도서 대출 기록 조회 요청 - periodType: {}, startDate: {}, endDate: {}, keyword: {}",
			periodType, startDate, endDate, keyword);

		validatePeriod(periodType, startDate, endDate);

		LocalDate start = (periodType == PeriodType.CUSTOM) ? startDate : periodType.getStartDate();
		LocalDate end = (periodType == PeriodType.CUSTOM) ? endDate : LocalDate.now();

		Page<BookHistoryResponse> response = bookHistoryService.getBookHistories(
			start, end, keyword, pageable);

		log.info("도서 대출 기록 조회 완료 - 총 {} 건", response.getTotalElements());

		return ResponseEntity.ok(response);
	}

	private void validatePeriod(PeriodType periodType, LocalDate startDate, LocalDate endDate) {
		if (periodType == PeriodType.CUSTOM) {
			if (startDate == null || endDate == null) {
				log.warn("CUSTOM 타입 조회 시 날짜 미입력");
				throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE_NO_DATE);
			}
			if (startDate.isAfter(endDate)) {
				log.warn("시작일이 종료일보다 늦음 - startDate: {}, endDate: {}", startDate, endDate);
				throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE_AFTER_DATE);
			}
		}
	}
}