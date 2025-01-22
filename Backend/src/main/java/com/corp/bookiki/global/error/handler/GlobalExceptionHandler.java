package com.corp.bookiki.global.error.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.error("[{}] {} - {}.{}",
			errorCode.name(),
			errorCode.getMessage(),
			ex.getStackTrace()[0].getClassName(),
			ex.getStackTrace()[0].getMethodName()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ErrorResponse.of(errorCode));
	}

	// Validation 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.error("MethodArgumentNotValidException: {}", ex.getMessage());
		List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> ErrorResponse.FieldError.builder()
				.field(error.getField())
				.value(String.valueOf(error.getRejectedValue()))
				.reason(error.getDefaultMessage())
				.build())
			.collect(Collectors.toList());

		return new ResponseEntity<>(
			ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors),
			HttpStatus.BAD_REQUEST
		);
	}

	// 나머지 예외 처리
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Exception: {}", ex.getMessage());
		return new ResponseEntity<>(
			ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR),
			HttpStatus.INTERNAL_SERVER_ERROR
		);
	}
}
