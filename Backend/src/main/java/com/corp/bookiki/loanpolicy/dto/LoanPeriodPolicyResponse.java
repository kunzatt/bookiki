package com.corp.bookiki.loanpolicy.dto;

import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "대출 기간 응답")
public class LoanPeriodPolicyResponse {
	@Schema(description = "대출 기간(일)", example = "14")
	private Integer loanPeriod;

	public LoanPeriodPolicyResponse(LoanPolicyEntity entity) {
		this.loanPeriod = entity.getLoanPeriod();
	}
}