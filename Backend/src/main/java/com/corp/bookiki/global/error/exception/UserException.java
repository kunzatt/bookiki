package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class UserException extends BusinessException {
	public UserException(ErrorCode errorCode) {
		super(errorCode);
	}
}
