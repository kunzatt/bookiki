package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class BookInformationException extends BusinessException {
	public BookInformationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
