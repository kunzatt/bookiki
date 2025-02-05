package com.corp.bookiki.loanpolicy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "대출 정책 수정 요청")
public class LoanPolicyRequest {
	@NotNull
	@Min(value = 1)
	@Schema(description = "최대 대출 가능 도서 수", example = "5", required = true)
	private Integer maxBooks;

	@NotNull
	@Min(value = 1)
	@Schema(description = "대출 기간(일)", example = "14", required = true)
	private Integer loanPeriod;
}
