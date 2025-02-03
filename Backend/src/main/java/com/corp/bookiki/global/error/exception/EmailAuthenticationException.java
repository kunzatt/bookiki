package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;
import lombok.Getter;

@Getter
public class EmailAuthenticationException extends BusinessException {
	public EmailAuthenticationException(ErrorCode errorCode) {
		super(errorCode);
	}
}