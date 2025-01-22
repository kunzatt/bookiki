package com.corp.bookiki.user.exception;

public class DuplicateCompanyIdException extends RuntimeException {
	public DuplicateCompanyIdException(String message) {
		super(message);
	}
}
