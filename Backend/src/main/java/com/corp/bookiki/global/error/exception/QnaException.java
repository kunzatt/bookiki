package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class QnaException extends BusinessException {
	public QnaException(ErrorCode errorCode) {
		super(errorCode);
	}
}
