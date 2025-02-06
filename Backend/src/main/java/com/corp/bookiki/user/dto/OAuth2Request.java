package com.corp.bookiki.user.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class OAuth2Request {

    protected Map<String, Object> attributes;

    public OAuth2Request(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getEmail();
    public abstract String getName();
}
