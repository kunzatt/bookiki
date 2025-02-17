package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Getter;

@Getter
public class QnaCommentException extends BusinessException {
	public QnaCommentException(ErrorCode errorCode) {
		super(errorCode);
	}
}
