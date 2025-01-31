package com.corp.bookiki.jwt.handler;

import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String TEMPORARY_TOKEN = "temporary_token";
    private static final String ADDITIONAL_INFO_PATH = "/oauth/google/additional";
    private static final String LOGIN_CALLBACK_PATH = "/login/callback";

    private final JWTService jwtService;
    private final CookieUtil cookieUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        log.debug("OAuth2 인증 성공 핸들러 시작");

        try {
            SecurityUserAdapter user = (SecurityUserAdapter) authentication.getPrincipal();
            log.debug("인증된 사용자 정보: {}", user.getEmail());

            // 신규 사용자 처리
            if (isNewUser(user)) {
                handleNewUser(response, user);
                return;
            }

            // 기존 사용자 처리
            handleExistingUser(response, user);

        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage());
            response.sendRedirect(frontendUrl + "/error");
        }
    }

    private boolean isNewUser(SecurityUserAdapter user) {
        return user.getAttributes() != null &&
                Boolean.TRUE.equals(user.getAttributes().get("isNewUser"));
    }

    private void handleNewUser(HttpServletResponse response, SecurityUserAdapter user) throws IOException {
        log.info("신규 사용자 감지: {}", user.getEmail());

        String temporaryToken = jwtService.generateTemporaryToken(user.getEmail());
        log.debug("임시 토큰 생성 완료");

        cookieUtil.addCookie(response, TEMPORARY_TOKEN, temporaryToken,
                (int) (jwtService.getTemporaryTokenExpiration() / 1000));
        log.debug("임시 토큰 쿠키 설정 완료");

        log.info("추가 정보 입력 페이지로 리다이렉트: {}", user.getEmail());
        response.sendRedirect(frontendUrl + ADDITIONAL_INFO_PATH);
    }

    private void handleExistingUser(HttpServletResponse response, SecurityUserAdapter user) throws IOException {
        log.info("기존 사용자 로그인 처리: {}", user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("토큰 생성 완료");

        cookieUtil.addCookie(response, ACCESS_TOKEN, accessToken);
        cookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken);
        log.debug("인증 토큰 쿠키 설정 완료");

        log.info("메인 페이지로 리다이렉트: {}", user.getEmail());
        response.sendRedirect(frontendUrl + LOGIN_CALLBACK_PATH);
    }
}
