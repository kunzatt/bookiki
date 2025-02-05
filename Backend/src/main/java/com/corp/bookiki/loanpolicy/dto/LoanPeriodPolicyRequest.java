package com.corp.bookiki.loanpolicy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "대출 기간 수정 요청")
public class LoanPeriodPolicyRequest {
	@NotNull
	@Min(value = 1)
	@Schema(description = "대출 기간(일)", example = "14", required = true)
	private Integer loanPeriod;
}