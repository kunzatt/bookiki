package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class ShelfException extends BusinessException {
	public ShelfException(ErrorCode errorCode) {
		super(errorCode);
	}
}
