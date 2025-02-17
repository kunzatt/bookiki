package com.corp.bookiki.loanpolicy.dto;

import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "대출 정책 응답")
public class LoanPolicyResponse {
	@Schema(description = "최대 대출 가능 도서 수", example = "5")
	private Integer maxBooks;

	@Schema(description = "대출 기간(일)", example = "14")
	private Integer loanPeriod;

	public LoanPolicyResponse(LoanPolicyEntity entity) {
		this.maxBooks = entity.getMaxBooks();
		this.loanPeriod = entity.getLoanPeriod();
	}
}
