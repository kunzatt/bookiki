package com.corp.bookiki.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TemporaryTokenRequest {
    private String temporaryToken;
}