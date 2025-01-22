package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class NoticeException extends BusinessException {
	public NoticeException(ErrorCode errorCode) {
		super(errorCode);
	}
}
