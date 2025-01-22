package com.corp.bookiki.global.error.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.corp.bookiki.global.error.code.ErrorCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
	private final LocalDateTime timestamp = LocalDateTime.now();
	private final int status;
	private final String message;

	private final List<FieldError> errors;

	@Getter
	@Builder
	public static class FieldError {
		private final String field;
		private final String value;
		private final String reason;
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus())
			.message(errorCode.getMessage())
			.errors(new ArrayList<>())
			.build();
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus())
			.message(errorCode.getMessage())
			.errors(errors)
			.build();
	}
}
