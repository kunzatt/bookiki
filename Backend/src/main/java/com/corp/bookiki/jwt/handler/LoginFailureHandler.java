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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.debug("로그인 실패 처리 시작");
        try {

            ErrorCode errorCode = getLoginErrorCode(exception);
            ErrorResponse errorResponse = ErrorResponse.of(errorCode);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), errorResponse);

            log.debug("로그인 실패 응답 완료");
        } catch (Exception e) {
            log.error("로그인 실패 처리 중 오류 발생: {}", e.getMessage());
            try {
                ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
            } catch (IOException ex) {
                log.error("응답 작성 중 오류 발생: {}", ex.getMessage());
            }
        }
    }

    private ErrorCode getLoginErrorCode(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return ErrorCode.LOGIN_BAD_CREDENTIALS;
        }
        return ErrorCode.LOGIN_FAILED;
    }
}
