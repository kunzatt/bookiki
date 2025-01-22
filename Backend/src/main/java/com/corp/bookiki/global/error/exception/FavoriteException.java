package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class FavoriteException extends BusinessException {
	public FavoriteException(ErrorCode errorCode) {
		super(errorCode);
	}
}
