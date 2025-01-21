package com.corp.bookiki.global.handler;

import com.corp.bookiki.user.exception.DuplicateEmailException;
import com.corp.bookiki.user.exception.DuplicateCompanyIdException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmailException(DuplicateEmailException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateCompanyIdException.class)
    public ResponseEntity<String> handleDuplicateCompanyIdException(DuplicateCompanyIdException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}