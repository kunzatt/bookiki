package com.corp.bookiki.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendationResponse {
    private List<RecommendedBook> recommendations;
    private String recommendationReason;
}
