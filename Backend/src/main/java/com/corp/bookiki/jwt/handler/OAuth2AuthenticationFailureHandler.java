package com.corp.bookiki.jwt.handler;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) {
        log.debug("OAuth2 인증 실패 처리 시작");

        try {
            ErrorCode errorCode = getOAuth2ErrorCode(exception);
            ErrorResponse errorResponse = ErrorResponse.of(errorCode);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(errorCode.getStatus());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), errorResponse);

            log.error("[{}] OAuth2 인증 실패: {}", errorCode.name(), exception.getMessage());
        } catch (Exception e) {
            log.error("OAuth2 인증 실패 처리 중 오류 발생: {}", e.getMessage());
            try {
                ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.OAUTH2_PROCESSING_ERROR);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
            } catch (IOException ex) {
                log.error("응답 작성 중 오류 발생: {}", ex.getMessage());
            }
        }
    }

    private ErrorCode getOAuth2ErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
            switch (error.getErrorCode()) {
                case "invalid_token":
                    return ErrorCode.OAUTH2_INVALID_TOKEN;
                case "invalid_request":
                    return ErrorCode.OAUTH2_INVALID_REQUEST;
                case "unauthorized_client":
                    return ErrorCode.OAUTH2_UNAUTHORIZED_CLIENT;
                case "access_denied":
                    return ErrorCode.OAUTH2_ACCESS_DENIED;
                default:
                    return ErrorCode.OAUTH2_PROCESSING_ERROR;
            }
        }
        return ErrorCode.OAUTH2_PROCESSING_ERROR;
    }
}