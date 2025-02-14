package com.corp.bookiki.jwt.filter;

import com.corp.bookiki.jwt.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("Request URL: {}", request.getRequestURL());

        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = resolveToken(request);
        if (!StringUtils.hasText(jwt)) {
            handleError(response, "토큰이 없습니다.");
            return;
        }

        try {
            jwtService.validateToken(jwt);  // 검증만 먼저 수행
            Authentication authentication = getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);  // 검증 성공시에만 필터 체인 진행
        } catch (ExpiredJwtException e) {
            log.debug("토큰이 만료되었습니다: {}", e.getMessage());
            handleTokenExpiredError(response);
            // 여기서 return하여 필터 체인 중단
        } catch (Exception e) {
            log.error("토큰 처리 중 오류: {}", e.getMessage());
            handleError(response, "유효하지 않은 토큰입니다.");
            // 여기서도 return하여 필터 체인 중단
        }
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/oauth2/") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/api/swagger-ui") ||
                path.startsWith("/api/user/signup") ||
                path.startsWith("/api/login") ||
                path.startsWith("/api/ws") ||
                path.startsWith("/api/password") ||
                path.startsWith("/api/books/return") ||
                path.startsWith("/api/iot-storage") ||
                path.startsWith("/api/user/login") ||
                path.startsWith("/api/api-docs") ||
                path.startsWith("/api/swagger-resources") ||
                path.startsWith("/api/configuration") ||
                path.startsWith("/api/webjars") ||
                path.startsWith("/iot/**") ||
                path.equals("/error");
    }

    private void handleTokenExpiredError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"token_expired\",\"message\":\"토큰이 만료되었습니다.\"}");
    }

    private void handleError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"error\":\"invalid_token\",\"message\":\"%s\"}", message));
    }

//    @Override
//    //HTTP 요청이 들어올 때마다 실행
//    //JWT 토큰 추출 → 검증 → Authentication 생성 순으로 처리
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        log.debug("Request URL: {}", request.getRequestURL());
//
//        // 쿠키 확인 로깅
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            log.debug("=== 수신된 쿠키 ===");
//            for (Cookie cookie : cookies) {
//                log.debug("쿠키 이름: {}, 값: {}", cookie.getName(), cookie.getValue());
//            }
//        } else {
//            log.debug("수신된 쿠키 없음!");
//        }
//
//        // 1. Request Header에서 JWT 토큰 추출
//        String jwt = resolveToken(request);
//
//        try {
//            // 2. validateToken으로 토큰 유효성 검사
//            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
//                // 3. 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
//                Authentication authentication = getAuthentication(jwt);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
//            }
//        } catch (Exception e) {
//            log.error("Cannot set user authentication: {}", e.getMessage());
//        }
//
//        filterChain.doFilter(request, response);
//    }

    // Authorization 헤더에서 Bearer 토큰을 추출
    // "Bearer " 접두사 제거
    private String resolveToken(HttpServletRequest request) {
        // 쿠키에서 토큰 찾기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    log.debug("Found access token in cookie");
                    return cookie.getValue();
                }
            }
        }
        log.debug("No access token found in cookies");
        return null;
    }

    // JWT 토큰에서 인증 정보 조회
    // JWT 토큰에서 사용자 이메일과 권한 정보를 추출
    // Authentication 객체 생성
    private Authentication getAuthentication(String token) {
        String email = jwtService.extractEmail(token);

        // 토큰에서 권한 정보 가져오기
        Collection<SimpleGrantedAuthority> authorities = Arrays.stream(
                        jwtService.extractAllClaims(token)
                                .get("authorities", String.class)
                                .split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }
}
