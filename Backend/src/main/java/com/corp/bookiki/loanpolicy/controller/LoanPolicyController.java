package com.corp.bookiki.loanpolicy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.loanpolicy.dto.LoanBookPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanBookPolicyResponse;
import com.corp.bookiki.loanpolicy.dto.LoanPeriodPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPeriodPolicyResponse;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyRequest;
import com.corp.bookiki.loanpolicy.dto.LoanPolicyResponse;
import com.corp.bookiki.loanpolicy.service.LoanPolicyService;
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
@RequestMapping("/api/loan-policy")
@RequiredArgsConstructor
@Tag(name = "대출 정책 API", description = "대출 정책 조회 및 수정 API")
@Slf4j
public class LoanPolicyController {

	private final LoanPolicyService loanPolicyService;

	@GetMapping
	@Operation(summary = "현재 대출 정책 조회", description = "현재 설정된 대출 정책을 조회합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "대출 정책 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LoanPolicyResponse.class)
			)
		)
	})
	public ResponseEntity<LoanPolicyResponse> getCurrentPolicy() {
		log.info("현재 대출 정책 조회");
		return ResponseEntity.ok(loanPolicyService.getCurrentPolicy());
	}

	@PatchMapping("/max-books")
	@Operation(summary = "최대 대출 가능 도서 수 수정", description = "최대로 대출할 수 있는 도서의 수를 수정합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "최대 대출 가능 도서 수 수정 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LoanBookPolicyResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "최대 대출 가능 도서 수는 1 이상이어야 합니다"
                        }
                        """
				)
			)
		)
	})
	public ResponseEntity<LoanBookPolicyResponse> updateMaxBooks(
		@Parameter(description = "수정할 최대 대출 가능 도서 수", required = true)
		@RequestBody LoanBookPolicyRequest request
	) {
		log.info("최대 대출 가능 도서 수 수정: {}", request.getMaxBooks());
		return ResponseEntity.ok(loanPolicyService.updateMaxBooks(request));
	}

	@PatchMapping("/loan-period")
	@Operation(summary = "대출 기간 수정", description = "도서 대출 가능 기간(일)을 수정합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "대출 기간 수정 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LoanPeriodPolicyResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "대출 기간은 1일 이상이어야 합니다"
                        }
                        """
				)
			)
		)
	})
	public ResponseEntity<LoanPeriodPolicyResponse> updateLoanPeriod(
		@Parameter(description = "수정할 대출 기간(일)", required = true)
		@RequestBody LoanPeriodPolicyRequest request
	) {
		log.info("대출 기간 수정: {}", request.getLoanPeriod());
		return ResponseEntity.ok(loanPolicyService.updateLoanPeriod(request));
	}

	@PutMapping
	@Operation(summary = "대출 정책 전체 수정", description = "대출 정책 전체(최대 대출 가능 도서 수, 대출 기간)를 수정합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "대출 정책 수정 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LoanPolicyResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "대출 정책 값은 1 이상이어야 합니다"
                        }
                        """
				)
			)
		)
	})
	public ResponseEntity<LoanPolicyResponse> updatePolicy(
		@Parameter(description = "수정할 대출 정책", required = true)
		@RequestBody LoanPolicyRequest request
	) {
		log.info("대출 정책 전체 수정 - 최대 도서 수: {}, 대출 기간: {}",
			request.getMaxBooks(), request.getLoanPeriod());
		return ResponseEntity.ok(loanPolicyService.updatePolicy(request));
	}
}