package com.corp.bookiki.global.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.corp.bookiki.user.exception.DuplicateCompanyIdException;
import com.corp.bookiki.user.exception.DuplicateEmailException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<String> handleDuplicateEmailException(DuplicateEmailException exception) {
		return ResponseEntity.badRequest().body(exception.getMessage());
	}

	@ExceptionHandler(DuplicateCompanyIdException.class)
	public ResponseEntity<String> handleDuplicateCompanyIdException(DuplicateCompanyIdException exception) {
		return ResponseEntity.badRequest().body(exception.getMessage());
	}
}
