package com.corp.bookiki.jwt.handler;


import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.util.CookieUtil;
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

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        log.debug("로그인 성공 핸들러 실행");

        SecurityUserAdapter user = (SecurityUserAdapter) authentication.getPrincipal();
        log.info("사용자 '{}' 로그인 성공", user.getEmail());

        try {
            // 토큰 생성
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            log.debug("JWT 토큰 생성 완료");

            // 쿠키 설정
            cookieUtil.addCookie(response, ACCESS_TOKEN, accessToken);
            cookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken);
            log.debug("쿠키 설정 완료");

            // 응답 설정
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}