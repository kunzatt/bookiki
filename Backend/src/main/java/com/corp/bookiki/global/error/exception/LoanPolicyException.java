package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class LoanPolicyException extends BusinessException {

	public LoanPolicyException(ErrorCode errorCode) {
		super(errorCode);
	}
}
