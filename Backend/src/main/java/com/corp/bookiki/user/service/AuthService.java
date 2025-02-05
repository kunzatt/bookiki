package com.corp.bookiki.user.service;

import com.corp.bookiki.global.error.exception.JWTException;
import com.corp.bookiki.jwt.repository.RefreshTokenRepository;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.LoginResponse;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Value("${cookie.secure-flag:true}")
    private boolean secureCookie;

    /**
     * Refresh Token을 Redis에 저장
     */
    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        refreshTokenRepository.save(email, refreshToken);
        log.debug("Refresh token saved for user: {}", email);
    }

    /**
     * 저장된 Refresh Token 검증
     */
    public boolean validateRefreshToken(String email, String refreshToken) {
        String savedToken = refreshTokenRepository.findByEmail(email);
        if (savedToken == null) {
            log.debug("No refresh token found for user: {}", email);
            return false;
        }

        boolean isValid = savedToken.equals(refreshToken) && jwtService.validateToken(refreshToken);
        log.debug("Refresh token validation result for user {}: {}", email, isValid);
        return isValid;
    }

    // 로그인 처리
    @Transactional
    public LoginResponse login(String username, String password, HttpServletResponse response) {
        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        // 2. Securitycontext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 3. JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(
                authentication.getName(),
                authentication.getAuthorities()
        );
        String refreshToken = jwtService.generateRefreshToken(
                authentication.getName()
        );
        //4. RefreshToken Redis에 저장
        saveRefreshToken(authentication.getName(), accessToken);
        // 5. 쿠키 설정
        setTokenCookies(response, accessToken, refreshToken);
        // 6. 응답 생성
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return LoginResponse.from(userDetails);
    }

    // 로그인시 쿠키 설정
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

     // 로그아웃 처리
    @Transactional
    public void logout(String email, HttpServletResponse response) {
        // 1. Redis에서 Refresh Token 삭제
        refreshTokenRepository.deleteAllTokens(email);
        // 2. SecurityContext 초기화
        SecurityContextHolder.clearContext();
        // 3. 쿠키 삭제
        deleteTokenCookies(response);

        log.debug("로그아웃 성공 - 유저 : {}", email);
    }

    // 쿠키 삭제 로직
    public void deleteTokenCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", "");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refresh_token", "");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    // Access Token 재발급
    @Transactional
    public String reissueAccessToken(String email, String refreshToken) {
        if (!validateRefreshToken(email, refreshToken)) {
            log.error("Invalid refresh token for user: {}", email);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(email, userDetails.getAuthorities());
        log.debug("Access token reissued for user: {}", email);
        return newAccessToken;
    }

    // Token 재발급
    @Transactional
    public void reissueTokens(String refreshToken, HttpServletResponse response) {
        // 1. Refresh Token에서 사용자 이메일 추출
        String email = jwtService.generateRefreshToken(refreshToken);
        // 2. Refresh Token 유효성 검증
        if(!validateRefreshToken(email, refreshToken)) {
            throw new JWTException("유효하지 않은 Refresh Token입니다.");
        }
        // 3. 새로운 Access Token 발급
        String newAccessToken = reissueAccessToken(email, refreshToken);
        // 4. 새로운 Refresh Token 발급 (RTR)
        String newRefreshToken = rotateRefreshToken(email, refreshToken);
        // 5. 쿠키 업데이트
        setTokenCookies(response, newAccessToken, newRefreshToken);

        log.debug("Tokens reissued for user: {}", email);
    }

    // Refresh Token 재발급 (RTR 전략)
    @Transactional
    public String rotateRefreshToken(String email, String oldRefreshToken) {
        if (!validateRefreshToken(email, oldRefreshToken)) {
            log.error("Invalid refresh token for rotation: {}", email);
            throw new IllegalArgumentException("Invalid refresh token for rotation");
        }

        String newRefreshToken = jwtService.generateRefreshToken(email);
        refreshTokenRepository.save(email, newRefreshToken);
        log.debug("Refresh token rotated for user: {}", email);
        return newRefreshToken;
    }

    // 현재 인증된 사용자의 정보 조회
    public UserEntity getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }


}