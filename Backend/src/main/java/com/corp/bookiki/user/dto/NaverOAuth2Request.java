package com.corp.bookiki.user.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class NaverOAuth2Request extends OAuth2Request {

    public NaverOAuth2Request(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("name");
    }
}
