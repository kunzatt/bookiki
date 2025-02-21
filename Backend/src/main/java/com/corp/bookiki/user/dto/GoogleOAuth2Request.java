package com.corp.bookiki.user.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleOAuth2Request extends OAuth2Request {

    public GoogleOAuth2Request(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
