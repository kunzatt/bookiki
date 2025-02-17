package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class BookHistoryException extends BusinessException {
	public BookHistoryException(ErrorCode errorCode) {
		super(errorCode);
	}
}
