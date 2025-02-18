package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class NotificationException extends BusinessException {
	public NotificationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
