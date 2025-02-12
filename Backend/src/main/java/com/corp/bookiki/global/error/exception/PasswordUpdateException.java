package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

public class PasswordUpdateException extends BusinessException {
	public PasswordUpdateException(ErrorCode errorCode) {
		super(errorCode);
	}
}
