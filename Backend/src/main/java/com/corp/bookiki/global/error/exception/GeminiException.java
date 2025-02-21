package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

public class GeminiException extends BusinessException {
    public GeminiException(ErrorCode errorCode) {
        super(errorCode);
    }
}
