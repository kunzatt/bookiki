package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class BookLocationException extends BusinessException {
	public BookLocationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
