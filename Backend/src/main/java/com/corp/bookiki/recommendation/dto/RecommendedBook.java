package com.corp.bookiki.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedBook {
    private Integer bookItemId;
    private String title;
    private String author;
}
