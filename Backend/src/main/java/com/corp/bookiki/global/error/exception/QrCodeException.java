package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class QrCodeException extends BusinessException {
	public QrCodeException(ErrorCode errorCode) {
		super(errorCode);
	}
}
