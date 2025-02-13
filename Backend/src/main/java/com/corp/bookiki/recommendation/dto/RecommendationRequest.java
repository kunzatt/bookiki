package com.corp.bookiki.recommendation.dto;

import com.corp.bookiki.bookhistory.enitity.PeriodType;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
public class RecommendationRequest {
    private final Integer limit = 5; // 추천 도서 개수 (기본값 5)

    // 내부적으로 고정된 기간 사용
    public LocalDate getStartDate() {
        return LocalDate.now().minusMonths(1);
    }

    public LocalDate getEndDate() {
        return LocalDate.now();
    }
}