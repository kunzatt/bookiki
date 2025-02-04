package com.corp.bookiki.bookhistory.dto;

import java.time.LocalDate;

import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "도서 대출 기록 조회 요청")
public class BookHistoryRequest {
	@Schema(
		description = "조회 기간 타입",
		example = "LAST_MONTH",
		required = true
	)
	@NotNull(message = "조회 기간 타입은 필수입니다")
	private PeriodType periodType;

	@Schema(
		description = "조회 시작일 (CUSTOM 타입일 때 필수)",
		example = "2024-01-01"
	)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(
		description = "조회 종료일 (CUSTOM 타입일 때 필수)",
		example = "2024-02-04"
	)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	public void validate() {
		if (periodType == PeriodType.CUSTOM) {
			if (startDate == null || endDate == null) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
					"사용자 정의 기간 조회 시 시작일과 종료일은 필수입니다");
			}
			if (startDate.isAfter(endDate)) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
					"시작일이 종료일보다 늦을 수 없습니다");
			}
		} else {
			startDate = periodType.getStartDate();
			endDate = LocalDate.now();
		}
	}
}