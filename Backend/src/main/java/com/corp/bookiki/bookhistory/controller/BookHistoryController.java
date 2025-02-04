package com.corp.bookiki.bookhistory.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.bookhistory.dto.BookHistoryRequest;
import com.corp.bookiki.bookhistory.dto.BookHistoryResponse;
import com.corp.bookiki.bookhistory.service.BookHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/book-histories")
@RequiredArgsConstructor
@Tag(name = "도서 대출 기록 API", description = "도서 대출 기록 조회 관련 API")
@Slf4j
public class BookHistoryController {
	private final BookHistoryService bookHistoryService;

	@GetMapping
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
        """
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					implementation = BookHistoryResponse.class
				),
				examples = @ExampleObject(
					value = """
                    {
                        "content": [
                            {
                                "id": 1,
                                "bookItemId": 100,
                                "userId": 200,
                                "borrowedAt": "2024-02-04T14:30:00",
                                "returnedAt": "2024-02-18T14:30:00"
                            }
                        ],
                        "pageable": {
                            "sort": {
                                "empty": false,
                                "sorted": true,
                                "unsorted": false
                            },
                            "offset": 0,
                            "pageNumber": 0,
                            "pageSize": 20,
                            "paged": true,
                            "unpaged": false
                        },
                        "totalElements": 1,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "sort": {
                            "empty": false,
                            "sorted": true,
                            "unsorted": false
                        },
                        "numberOfElements": 1,
                        "first": true,
                        "empty": false
                    }
                    """
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = """
            잘못된 요청
            - 필수 파라미터 누락
            - CUSTOM 타입일 때 시작일/종료일 누락
            - 시작일이 종료일보다 늦은 경우
            """,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                    {
                        "timestamp": "2024-02-04T14:30:00",
                        "status": 400,
                        "message": "잘못된 입력값입니다",
                        "errors": []
                    }
                    """
				)
			)
		)
	})
	public ResponseEntity<Page<BookHistoryResponse>> getBookHistories(
		@Parameter(
			description = "도서 대출 기록 조회 조건",
			required = true
		)
		@Valid @ModelAttribute BookHistoryRequest request,
		@Parameter(
			description = "페이지네이션 정보 (기본값: size=20, sort=borrowedAt,desc)",
			required = false
		)
		@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		log.info("도서 대출 기록 조회 요청: periodType={}, startDate={}, endDate={}",
			request.getPeriodType(),
			request.getStartDate(),
			request.getEndDate());

		return ResponseEntity.ok(bookHistoryService.getBookHistories(request, pageable));
	}
}