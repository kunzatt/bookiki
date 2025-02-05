package com.corp.bookiki.loanpolicy.entity;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.LoanPolicyException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "loan_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanPolicyEntity {

	@Id
	private Integer id;

	@Column(nullable = false)
	private Integer maxBooks;

	@Column(nullable = false)
	private Integer loanPeriod;

	@Builder
	public LoanPolicyEntity(Integer maxBooks, Integer loanPeriod) {
		this.maxBooks = maxBooks;
		this.loanPeriod = loanPeriod;
	}

	public void updatePolicy(Integer maxBooks, Integer loanPeriod) {
		this.maxBooks = maxBooks;
		this.loanPeriod = loanPeriod;
	}

	public void updateMaxBooks(Integer maxBooks) {
		validateMaxBooks(maxBooks);
		this.maxBooks = maxBooks;
	}

	public void updateLoanPeriod(Integer loanPeriod) {
		validateLoanPeriod(loanPeriod);
		this.loanPeriod = loanPeriod;
	}

	private void validateMaxBooks(Integer maxBooks) {
		if (maxBooks == null || maxBooks < 1) {
			throw new LoanPolicyException(ErrorCode.INVALID_MAX_BOOKS);
		}
	}

	private void validateLoanPeriod(Integer loanPeriod) {
		if (loanPeriod == null || loanPeriod < 1) {
			throw new LoanPolicyException(ErrorCode.INVALID_LOAN_PERIOD);
		}
	}
}
