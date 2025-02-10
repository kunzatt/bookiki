package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;

public class FileException extends BusinessException {
    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }
}
