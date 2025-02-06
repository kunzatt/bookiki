package com.corp.bookiki.user.service;

import com.corp.bookiki.global.error.exception.JWTException;
import com.corp.bookiki.jwt.properties.JwtProperties;
import com.corp.bookiki.jwt.repository.RefreshTokenRepository;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.LoginResponse;
import com.corp.bookiki.user.dto.OAuth2SignUpRequest;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

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
        try {
            log.info("Login attempt for user: {}", username);

            // 1. 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 2. 인증 실패 시 예외 발생 (authenticate 메서드가 실패하면 여기까지 오지 않음)
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("Authentication failed for user: {}", username);
                throw new BadCredentialsException("Authentication failed");
            }

            log.debug("인증 성공 - user: {}, authorities: {}",
                    authentication.getName(),
                    authentication.getAuthorities());

            // 3. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. JWT 토큰 생성
            String accessToken = jwtService.generateAccessToken(
                    authentication.getName(),
                    authentication.getAuthorities()
            );
            String refreshToken = jwtService.generateRefreshToken(
                    authentication.getName()
            );
            log.debug("토큰 생성 완료 - access token length: {}", accessToken.length());

            // 5. RefreshToken Redis에 저장
            saveRefreshToken(authentication.getName(), accessToken);
            log.debug("Redis에 refresh token 저장 완료");

            // 6. 쿠키 설정
            setTokenCookies(response, accessToken, refreshToken);
            log.debug("쿠키 설정 완료");

            // 7. 응답 생성
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.debug("로그인 프로세스 완료 - user: {}", userDetails.getUsername());

            return LoginResponse.from(userDetails);

        } catch (BadCredentialsException e) {
            log.error("인증 실패 - 잘못된 자격증명: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException ex) {
            log.error("인증 실패 - 인증 예외: {}", ex.getMessage());
            throw new AuthenticationException("Authentication failed") {};
        } catch (Exception ex) {
            log.error("로그인 처리 중 예외 발생", ex);
            throw new AuthenticationException("Login failed: " + ex.getMessage()) {};
        }
    }

    // 로그인시 쿠키 설정
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 60);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);

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
    private void deleteTokenCookies(HttpServletResponse response) {
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
        if (!validateRefreshToken(email, refreshToken)) {
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

    // OAuth2 회원가입 처리
    @Transactional
    public void completeOAuth2SignUp(String temporaryToken, OAuth2SignUpRequest request, HttpServletResponse response) {
        // 1. 임시 토큰 검증 및 정보 추출
        if (!jwtService.validateToken(temporaryToken)) {
            throw new JWTException("유효하지 않은 임시 토큰입니다.");
        }
        Claims claims = jwtService.extractAllClaims(temporaryToken);
        String email = claims.getSubject().replace(jwtProperties.getSubjectPrefix(), "");
        Provider provider = Provider.valueOf(claims.get("provider", String.class));

        // 2. 회원 정보 검증(추가 정보 입력)
        if (userRepository.existsByCompanyId(request.getCompanyId())) {
            throw new IllegalArgumentException("이미 사용 중인 사번입니다.");
        }
        // 3. 회원 정보 저장
        UserEntity user = UserEntity.builder()
                .email(email)
                .userName(request.getUserName())
                .companyId(request.getCompanyId())
                .provider(provider)
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // 4. JWT 토큰 발급
        String accessToken = jwtService.generateAccessToken(
                email,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + Role.USER.name()))
        );
        String refreshToken = jwtService.generateRefreshToken(email);

        // 5. Refresh Token Redis 저장
        saveRefreshToken(email, refreshToken);

        // 6. 쿠키에 토큰 저장
        setTokenCookies(response, accessToken, refreshToken);

        log.debug("OAuth2 회원가입 완료: {}", email);
    }
}