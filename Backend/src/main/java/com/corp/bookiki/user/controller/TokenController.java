package com.corp.bookiki.user.controller;

import com.corp.bookiki.jwt.dto.TemporaryTokenRequest;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.LoginResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public ResponseEntity<LoginResponse> setToken(@RequestBody TemporaryTokenRequest request, HttpServletResponse response) {
        String temporaryToken = request.getTemporaryToken();

        // 임시 토큰 검증
        if (!jwtService.validateToken(temporaryToken)) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        // 토큰에서 이메일 추출
        String email = jwtService.extractEmail(temporaryToken);

        // 유저 정보 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 실제 access token과 refresh token 생성
        String accessToken = jwtService.generateAccessToken(
                email,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        String refreshToken = jwtService.generateRefreshToken(email);

        // Redis에 refresh token 저장
        authService.saveRefreshToken(email, refreshToken);

        // 쿠키에 토큰 저장
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 60);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // LoginResponse 형식으로 응답
        return ResponseEntity.ok(LoginResponse.builder()
                .id(user.getId())
                .username(user.getUserName())
                .role(user.getRole())
                .build());
    }
}