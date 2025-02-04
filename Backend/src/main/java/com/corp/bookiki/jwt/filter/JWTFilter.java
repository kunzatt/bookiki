package com.corp.bookiki.jwt.filter;

import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.util.CookieUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
	private final JwtTokenProvider tokenProvider;
	private final CookieUtil cookieUtil;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final List<String> excludedUrls = Arrays.asList(
		"/api/auth/**",
		"/api/email/**",
		"/api/login/**",
		"/api/signup/**",
		"/favicon.ico",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/api/test-token/**"
	);

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		log.debug("JWT 검증 필터 시작 - URI: {}", request.getRequestURI());

		String accessToken = extractTokenFromCookies(request.getCookies(), ACCESS_TOKEN);
		String refreshToken = extractTokenFromCookies(request.getCookies(), REFRESH_TOKEN);

		try {
			if (refreshToken != null) {
				if (accessToken == null || jwtService.isTokenExpired(accessToken)) {
					handleExpiredAccessToken(refreshToken, response);
				} else {
					processAccessToken(accessToken);
				}
			}
			filterChain.doFilter(request, response);
		} catch (JwtException e) {
			log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		log.debug("JWT 검증 필터 완료");
	}

	private String extractTokenFromCookies(Cookie[] cookies, String tokenName) {
		if (cookies == null)
			return null;

		return Arrays.stream(cookies)
			.filter(cookie -> tokenName.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElseGet(() -> {
				log.debug("쿠키에 {} 토큰이 없음", tokenName);
				return null;
			});
	}

	private void processAccessToken(String accessToken) {
		if (SecurityContextHolder.getContext().getAuthentication() != null)
			return;

		Authentication authentication = tokenProvider.getAuthentication(accessToken);
		if (authentication != null) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	}

	private void handleExpiredAccessToken(String refreshToken, HttpServletResponse response) {
		try {
			String userEmail = jwtService.extractUserEmail(refreshToken);
			UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

			// UserDetails를 SecurityUserAdapter로 캐스팅
			if (!(userDetails instanceof SecurityUserAdapter)) {
				throw new AuthenticationException("Invalid user type") {
				};
			}

			SecurityUserAdapter securityUser = (SecurityUserAdapter)userDetails;

			Map<String, String> tokens = jwtService.rotateTokens(refreshToken, securityUser);
			if (tokens != null) {
				setTokenCookies(tokens, response);
				processAccessToken(tokens.get("accessToken"));
			} else {
				throw new AuthenticationException("토큰 갱신 실패") {
				};
			}
		} catch (Exception ex) {
			log.error("토큰 갱신 실패: {}", ex.getMessage());
			throw new JwtException("토큰 갱신 중 오류 발생");
		}
	}

	private void setTokenCookies(Map<String, String> tokens, HttpServletResponse response) {
		cookieUtil.addCookie(response, ACCESS_TOKEN, tokens.get("accessToken"));
		cookieUtil.addCookie(response, REFRESH_TOKEN, tokens.get("refreshToken"));
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		// 각 패턴별 매칭 결과를 로깅
		excludedUrls.forEach(pattern -> {
			boolean matches = pathMatcher.match(pattern, path);
			log.debug("패턴 매칭 검사 - Pattern: {}, URI: {}, 매칭 결과: {}", pattern, path, matches);
		});

		boolean isExcluded = excludedUrls.stream()
				.anyMatch(pattern -> pathMatcher.match(pattern, path));

		log.debug("JWT 필터 체크 - URI: {}, 제외 여부: {}", path, isExcluded);
		return isExcluded;
	}

}
