package com.corp.bookiki.jwt.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.jwt.JwtTokenProvider;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWTService {

    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.temporary-token.expiration}")
    private long temporaryTokenExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    // Access Token 생성
    public String generateAccessToken(Object principal) {
        Authentication auth;
        if (principal instanceof SecurityUserAdapter) {
            auth = createAuthentication((SecurityUserAdapter)principal);
        } else if (principal instanceof UserEntity) {
            auth = createAuthentication(new SecurityUserAdapter((UserEntity)principal));
        } else {
            throw new IllegalArgumentException("Unsupported principal type");
        }
        return tokenProvider.createAccessToken(auth);
    }

    // Refresh Token 생성 및 Redis 저장
    public String generateRefreshToken(Object principal) {
        String email;
        if (principal instanceof SecurityUserAdapter) {
            email = ((SecurityUserAdapter)principal).getEmail();
        } else if (principal instanceof UserEntity) {
            email = ((UserEntity)principal).getEmail();
        } else {
            throw new IllegalArgumentException("Unsupported principal type");
        }

        Authentication auth = createAuthentication(new SecurityUserAdapter(
                principal instanceof UserEntity ? (UserEntity)principal
                        : ((SecurityUserAdapter)principal).getUser()
        ));
        String refreshToken = tokenProvider.generateToken(auth, refreshTokenExpiration);
        saveRefreshToken(email, refreshToken);
        return refreshToken;
    }

    // 임시 토큰 생성 (이메일 인증 등에 사용)
    public String generateTemporaryToken(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, null);
        return tokenProvider.generateToken(auth, temporaryTokenExpiration);
    }

    // Authentication 객체 생성 헬퍼 메서드
    private Authentication createAuthentication(SecurityUserAdapter user) {
        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    // 토큰 검증
    public boolean isValid(String token, UserDetails user) {
        try {
            String userEmail = extractUserEmail(token);
            if (!userEmail.equals(user.getUsername())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "토큰의 사용자 정보가 일치하지 않습니다.");
            }
            if (isTokenExpired(token)) {
                throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
            }
            return tokenProvider.validateToken(token);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "토큰 검증 중 오류가 발생했습니다.");
        }
    }

    // Refresh Token 검증
    public boolean isValidRefreshToken(String token, SecurityUserAdapter user) {
        try {
            if (!isValid(token, createUserDetails(user))) {
                return false;
            }

            String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + user.getEmail());
            if (storedToken == null) {
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }
            if (!token.equals(storedToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }
            return true;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰 검증 중 오류가 발생했습니다.");
        }
    }

    // UserDetails 생성 헬퍼 메서드
    private UserDetails createUserDetails(Object principal) {
        String email;
        Role role;

        if (principal instanceof SecurityUserAdapter) {
            SecurityUserAdapter adapter = (SecurityUserAdapter)principal;
            email = adapter.getEmail();
            role = adapter.getRole();
        } else if (principal instanceof UserEntity) {
            UserEntity user = (UserEntity)principal;
            email = user.getEmail();
            role = user.getRole();
        } else {
            throw new IllegalArgumentException("Unsupported principal type");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
                .build();
    }

    // Redis 관련 메서드들
    private void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    public void invalidateRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }

    // 토큰 갱신(RTR)
    public Map<String, String> rotateTokens(String oldRefreshToken, SecurityUserAdapter user) {
        try {
            if (!isValidRefreshToken(oldRefreshToken, user)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }

            invalidateRefreshToken(user.getEmail());

            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = generateRefreshToken(user);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            return tokens;
        } catch (Exception e) {
            invalidateRefreshToken(user.getEmail());
            throw e;
        }
    }

    // 토큰 정보 추출 메서드들
    public String extractUserEmail(String token) {
        return tokenProvider.parseClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = tokenProvider.parseClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public AuthUser getUserInfoFromToken(String token) {
        try {
            Claims claims = tokenProvider.parseClaims(token);
            return AuthUser.builder()
                    .id(claims.get("userId", Integer.class))
                    .email(claims.getSubject())
                    .role(Role.valueOf(claims.get("auth", String.class).replace("ROLE_", "")))
                    .build();
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_TOKEN,
                    "토큰에서 사용자 정보를 추출할 수 없습니다."
            );
        }
    }

    // Getter 메서드들
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public long getTemporaryTokenExpiration() {
        return temporaryTokenExpiration;
    }
}