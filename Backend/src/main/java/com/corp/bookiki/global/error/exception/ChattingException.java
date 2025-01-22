package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class ChattingException extends BusinessException {
	public ChattingException(ErrorCode errorCode) {
		super(errorCode);
	}
}
