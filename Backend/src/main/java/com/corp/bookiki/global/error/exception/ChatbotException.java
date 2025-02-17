package com.corp.bookiki.global.error.exception;

import com.corp.bookiki.global.error.code.ErrorCode;
import lombok.Getter;

@Getter
public class ChatbotException extends BusinessException {
    public ChatbotException(ErrorCode errorCode) {
        super(errorCode);
    }
}
