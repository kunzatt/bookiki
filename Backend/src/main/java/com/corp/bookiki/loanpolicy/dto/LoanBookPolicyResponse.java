package com.corp.bookiki.loanpolicy.dto;

import com.corp.bookiki.loanpolicy.entity.LoanPolicyEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "최대 대출 가능 도서 수 응답")
public class LoanBookPolicyResponse {
	@Schema(description = "최대 대출 가능 도서 수", example = "5")
	private Integer maxBooks;

	public LoanBookPolicyResponse(LoanPolicyEntity entity) {
		this.maxBooks = entity.getMaxBooks();
	}
}