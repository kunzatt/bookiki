package com.corp.bookiki.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class CookieUtil {

    @Value("${cookie.secure-flag:false}")
    private boolean secureCookie;

    // 기존 메서드
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        log.debug("쿠키 생성 시작 - 이름: {}, 만료시간: {}초", name, maxAge);

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(secureCookie)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("쿠키 생성 완료 - 설정: path=/, sameSite=Lax, httpOnly=true, secure={}", secureCookie);
    }

    // maxAge 없는 버전 추가
    public void addCookie(HttpServletResponse response, String name, String value) {
        addCookie(response, name, value, 3600); // 기본값 1시간
    }

    public void removeCookie(HttpServletResponse response, String name) {
        log.debug("쿠키 삭제 시작 - 이름: {}", name);

        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .sameSite("Lax")
                .httpOnly(true)
                .secure(secureCookie)
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("쿠키 삭제 완료 - 이름: {}", name);
    }

    public String extractTokenFromCookies(HttpServletRequest request, String tokenName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> tokenName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("쿠키에서 {} 를 찾을 수 없습니다.", tokenName);
                    return null;
                });
    }
}
