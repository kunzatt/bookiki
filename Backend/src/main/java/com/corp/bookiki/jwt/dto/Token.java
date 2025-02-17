package com.corp.bookiki.jwt.dto;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class Token {
    private String accessToken;
}
