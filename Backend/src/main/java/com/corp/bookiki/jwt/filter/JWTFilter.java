package com.corp.bookiki.jwt.filter;

import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.SecurityUser;
import com.corp.bookiki.util.CookieUtil;
import com.corp.bookiki.util.JWTUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private final UserDetailsService userDetailsService;
    private final JWTService jwtService;
    private final JWTUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> excludedUrls = Arrays.asList(
            "/api/auth/**",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.debug("JWT 검증 필터 시작 - URI: {}", request.getRequestURI());

        String accessToken = extractTokenFromCookies(request.getCookies(), ACCESS_TOKEN);
        String refreshToken = extractTokenFromCookies(request.getCookies(), REFRESH_TOKEN);

        try{
            if(refreshToken != null){
                if (accessToken == null || isTokenExpired(accessToken)) {
                    handleExpiredToken(refreshToken, response);
                    return;
                }
                if (accessToken != null) {
                    processAccessToken(accessToken, response);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        catch (JwtException e) {
            log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        log.debug("JWT 검증 필터 완료");
        filterChain.doFilter(request, response);

    }

    private String extractTokenFromCookies(Cookie[] cookies, String token) {
        if(cookies == null) return null;
        for (Cookie cookie : cookies) {
            if(token.equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        log.debug("쿠키에 {} 토큰이 없음", token);
        return null;
    }

    private void processAccessToken(String accessToken, HttpServletResponse response) {
        String userEmail = jwtService.extractUserEmail(accessToken);

        if (userEmail == null) return;
        if (SecurityContextHolder.getContext().getAuthentication() != null) return;

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        if (jwtService.isValid(accessToken, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private void handleExpiredToken(String refreshToken, HttpServletResponse response) {
        try {
            String userEmail = jwtService.extractUserEmail(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if(!(userDetails instanceof SecurityUser )){
                throw new AuthenticationException("유효하지 않은 사용자 정보 타입입니다");
            }

            SecurityUser user = ((SecurityUser ) userDetails).getUser();
            Map<String, String> tokens = jwtService.rotateTokens(refreshToken, user);

            if (tokens != null) {
                setTokenCookies(tokens, response);
                processAccessToken(tokens.get("accessToken"), response);
                return;
            }
        }
        catch (Exception ex){
            log.error("토큰 갱신 실패: {}", ex.getMessage());
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
    }

    private void setTokenCookies(Map<String, String> tokens, HttpServletResponse response) {
        jwtUtil.addCookie(response, ACCESS_TOKEN, tokens.get("accessToken"),
                (int) (jwtService.getAccessTokenExpire() / 1000));
        jwtUtil.addCookie(response, REFRESH_TOKEN, tokens.get("refreshToken"),
                (int) (jwtService.getRefreshTokenExpire() / 1000));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean isExcluded = excludedUrls.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        log.debug("JWT 필터 체크 - URI: {}, 제외 여부: {}", path, isExcluded);
        return isExcluded;
    }

}
