package com.corp.bookiki.jwt.controller;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.util.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/test-token")
@Tag(name = "Test Token", description = "테스트용 토큰 발급 API")
@RequiredArgsConstructor
@Slf4j
public class TestTokenController {

	private final JwtTokenProvider jwtTokenProvider;
	private final CookieUtil cookieUtil;

	private static final UserEntity TEST_USER = UserEntity.builder()
		.id(3)
		.email("user-test@bookiki.com")
		.userName("user-test")
		.companyId("bookikiuser")
		.role(Role.USER)
		.deleted(false)
		.build();

	private static final UserEntity TEST_ADMIN = UserEntity.builder()
		.id(4)
		.email("admin-test@bookiki.com")
		.userName("admin-test")
		.companyId("bookikiadmin")
		.role(Role.ADMIN)
		.deleted(false)
		.build();

	@Operation(summary = "사용자 테스트 토큰 발급", description = "일반 사용자 권한의 테스트용 JWT 토큰을 쿠키에 발급합니다.")
	@GetMapping("/user")
	public ResponseEntity<String> getUserToken(HttpServletResponse response) {
		cleanupExistingTokens(response);
		issueTestTokens(response, TEST_USER);
		return ResponseEntity.ok("User test tokens issued to cookies (ID: 1)");
	}

	@Operation(summary = "관리자 테스트 토큰 발급", description = "관리자 권한의 테스트용 JWT 토큰을 쿠키에 발급합니다.")
	@GetMapping("/admin")
	public ResponseEntity<String> getAdminToken(HttpServletResponse response) {
		cleanupExistingTokens(response);
		issueTestTokens(response, TEST_ADMIN);
		return ResponseEntity.ok("Admin test tokens issued to cookies (ID: 2)");
	}

	private void issueTestTokens(HttpServletResponse response, UserEntity user) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			user.getEmail(),
			null,
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
		);

		// JWTService를 사용하지 않고 직접 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(authentication);
		String refreshToken = jwtTokenProvider.generateToken(authentication, 604800000); // 7일

		cookieUtil.addCookie(response, "access_token", accessToken);
		cookieUtil.addCookie(response, "refresh_token", refreshToken);
	}

	private void cleanupExistingTokens(HttpServletResponse response) {
		cookieUtil.removeCookie(response, "access_token");
		cookieUtil.removeCookie(response, "refresh_token");
	}

	@Operation(summary = "테스트 토큰 삭제", description = "발급된 테스트용 JWT 토큰을 삭제합니다.")
	@GetMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response) {
		cleanupExistingTokens(response);
		return ResponseEntity.ok("Test tokens removed from cookies");
	}
}