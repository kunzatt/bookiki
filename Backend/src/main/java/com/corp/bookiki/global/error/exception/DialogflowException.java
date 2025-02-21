package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;
import lombok.Getter;

@Getter
public class DialogflowException extends BusinessException {
    public DialogflowException(ErrorCode errorCode) {
        super(errorCode);
    }
}
