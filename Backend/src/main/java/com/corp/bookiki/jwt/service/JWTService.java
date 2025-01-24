package com.corp.bookiki.jwt.service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${JWT_ACCESS_VALIDITY}") // 액세스 토큰 만료시간
    private long accessTokenExpire;

    @Value("${JWT_REFRESH_VALIDITY}") // 리프레시 토큰 만료시간
    private long refreshTokenExpire;

    @Value("${JWT_TEMPORARY_VALIDITY}") // 임시 토큰 만료시간
    private long temporaryTokenExpire;

    private final RedisTemplate<String, String> redisTemplate; // 레디스 연동
    private static final String REFRESH_TOKEN_PREFIX = "RT:"; // // Redis에 저장될 리프레시 토큰의 접두어

    // 토큰 생성
    // 액세스 토큰 생성
    public String generateAccessToken(SecurityUser user) {
        return makeToken(user, accessTokenExpire);
    }

    // 리프레시 토큰 생성 및 Redis에 저장
    public String generateRefreshToken(SecurityUser user) {
        String refreshToken = makeToken(user, refreshTokenExpire);
        saveRefreshToken(user.getEmail(), refreshToken);
        return refreshToken;
    }

    // 임시 토큰 생성 (이메일 인증 등에 사용)
    public String generateTemporaryToken(String email) {
        String temporaryToken = generateTempToken(email, temporaryTokenExpire);
        return temporaryToken;
    }

    // 임시 토큰 생성 로직
    private String generateTempToken(String email, long expireTime) {
        Date now = new Date();
        return Jwts
                .builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireTime))
                .signWith(getSigninKey())
                .compact();
    }

    // JWT 토큰 생성
    private String makeToken(SecurityUser user, long expireTime) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId()) // 토큰에 id 저장
                .claim("role", user.getRole()) // 토큰에 role 저장
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireTime))
                .signWith(getSigninKey())
                .compact();
    }


    // 유효성 검사
    public boolean isValid(String token, UserDetails user) {
        try {
            String userEmail = extractUserEmail(token);
            if (!userEmail.equals(user.getUsername())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN,"토큰의 사용자 정보가 일치하지 않습니다.");
            }
            if (isTokenExpired(token)) {
                throw new BusinessException( ErrorCode.EXPIRED_TOKEN);
            }
            return true;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN,"토큰 검증 중 오류가 발생했습니다.");
        }
    }

    // 토큰 만료 여부 확인
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 토큰의 만료 시간 추출
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 리프레시 토큰 유효성 검사
    public boolean isValidRefreshToken(String token, SecurityUser user) {
        try {
            String userEmail = extractUserEmail(token);
            if (!userEmail.equals(user.getEmail())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN,"리프레시 토큰의 사용자 정보가 일치하지 않습니다.");
            }
            if (isTokenExpired(token)) {
                throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
            }

            String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + user.getEmail());
            if (storedToken == null) {
                throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }
            if (!token.equals(storedToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN,"유효하지 않은 리프레시 토큰입니다.");
            }
            return true;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN,"리프레시 토큰 검증 중 오류가 발생했습니다.");
        }
    }


    // 토큰에서 정보 가져오는 메서드
    // 토큰에서 특정 클레임 추출
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // 유저 이메일 가져오는 메서드
    public String extractUserEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰에서 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }






    // 리프레시 토큰 관리
    // 리프레시 토큰을 Redis에 저장
    private void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                refreshToken,
                refreshTokenExpire,
                TimeUnit.MILLISECONDS
        );
    }

    // JWT 서명에 사용할 키 생성
    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 리프레시 토큰 RTR
    public Map<String, String> rotateTokens(String oldRefreshToken, SecurityUser user) {
        try {
            if (!isValidRefreshToken(oldRefreshToken, user)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN,"유효하지 않은 리프레시 토큰입니다.");
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

    // Redis에서 리프레시 토큰 삭제
    public void invalidateRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }


    // 토큰의 만료 시간을 반환하는 getter
    // AccessToken 만료 시간
    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }

    // RefreshToken 만료 시간
    public long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }

    // TemporaryToken 만료 시간
    public long getTemporaryTokenExpire() {
        return temporaryTokenExpire;
    }


    // 토큰에서 정보를 추출하는 메서드들
    // userId
    public int getUserIdFromToken(String token) {
        return extractAllClaims(token).get("userId", Integer.class);
    }

    // Email
    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    // role
    public String getRoleFromToken(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // userInformation
    public AuthUser getUserInfoFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return AuthUser.builder()
                    .id(claims.get("userId", Integer.class))
                    .email(claims.getSubject())
                    .role(claims.get("role", Role.class))
                    .build();
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_TOKEN,
                    "토큰에서 사용자 정보를 추출할 수 없습니다."
            );
        }
    }
}

