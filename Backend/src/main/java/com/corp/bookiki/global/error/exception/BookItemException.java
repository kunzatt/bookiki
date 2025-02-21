package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class BookItemException extends BusinessException {
	public BookItemException(ErrorCode errorCode) {
		super(errorCode);
	}
}
