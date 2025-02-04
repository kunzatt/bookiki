package com.corp.bookiki.bookhistory.enitity;

import java.time.LocalDate;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BookHistoryException;

import lombok.Getter;

@Getter
public enum PeriodType {
	LAST_WEEK("최근 1주일"),
	LAST_MONTH("최근 1개월"),
	LAST_THREE_MONTHS("최근 3개월"),
	LAST_SIX_MONTHS("최근 6개월"),
	LAST_YEAR("최근 1년"),
	CUSTOM("사용자 지정");

	private final String description;

	PeriodType(String description) {
		this.description = description;
	}

	public LocalDate getStartDate() {
		return switch (this) {
			case LAST_WEEK -> LocalDate.now().minusWeeks(1);
			case LAST_MONTH -> LocalDate.now().minusMonths(1);
			case LAST_THREE_MONTHS -> LocalDate.now().minusMonths(3);
			case LAST_SIX_MONTHS -> LocalDate.now().minusMonths(6);
			case LAST_YEAR -> LocalDate.now().minusYears(1);
			case CUSTOM -> throw new BookHistoryException(ErrorCode.INVALID_INPUT_VALUE);
		};
	}
}