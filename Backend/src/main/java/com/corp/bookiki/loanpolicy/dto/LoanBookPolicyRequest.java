package com.corp.bookiki.loanpolicy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "최대 대출 가능 도서 수 수정 요청")
public class LoanBookPolicyRequest {
	@NotNull
	@Min(value = 1)
	@Schema(description = "최대 대출 가능 도서 수", example = "5", required = true)
	private Integer maxBooks;
}