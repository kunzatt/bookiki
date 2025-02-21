package com.corp.bookiki.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final String frontendUrl;

    public OAuth2AuthenticationFailureHandler(@Value("${FRONTEND_URL}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.debug("OAuth2AuthenticationFailureHandler/onAuthenticationFailure");
        log.debug("OAuth2 인증 실패: error={}", exception.getMessage());
        if (exception instanceof OAuth2AuthenticationException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
