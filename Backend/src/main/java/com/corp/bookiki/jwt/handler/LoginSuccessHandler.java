package com.corp.bookiki.jwt.handler;


import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.dto.LoginResponse;
import com.corp.bookiki.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final JWTService jwtService;
    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper;    //로그인 응답 JSON으로 변형

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        log.info("로그인 성공 핸들러 실행 시작");
        log.info("인증 객체: {}", authentication);

        try {
            SecurityUserAdapter user = (SecurityUserAdapter) authentication.getPrincipal();
            log.info("사용자 이메일: {}", user.getEmail());

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("액세스 토큰 생성: {}", accessToken);
            log.info("리프레시 토큰 생성: {}", refreshToken);

            cookieUtil.addCookie(response, ACCESS_TOKEN, accessToken);
            cookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken);

            LoginResponse loginResponse = LoginResponse.from(user);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(), loginResponse);

            log.info("로그인 응답 전송 완료");

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            throw e;
        }
    }
}