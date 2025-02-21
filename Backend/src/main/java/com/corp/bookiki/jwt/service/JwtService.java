package com.corp.bookiki.jwt.service;

import com.corp.bookiki.jwt.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import com.corp.bookiki.user.entity.Provider;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // JWT 서명을 위한 키 생성
    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String generateAccessToken(String email, Collection<? extends GrantedAuthority> authorities) {
        Date now = new Date();
        Date expiryDate = calculateExpirationDate(jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .setSubject(jwtProperties.getSubjectPrefix() + email)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .signWith(getSigningKey())
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = calculateExpirationDate(jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .setSubject(jwtProperties.getSubjectPrefix() + email)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Temporary Token 생성
    public String generateTemporaryToken(String email, Provider provider) {
        Date now = new Date();
        Date expiryDate = calculateExpirationDate(jwtProperties.getTemporaryTokenExpiration());

        return Jwts.builder()
                .setSubject(email)
                .setIssuer(jwtProperties.getIssuer())
                .claim("provider", provider.name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Token 검증
    public boolean validateToken(String token) {
        try {
            log.debug("토큰 검증 시도. 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

            if (token == null) {
                log.debug("토큰이 null입니다");
                return false;
            }

            // 토큰 만료시간 체크
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            log.debug("토큰 정보 - Subject: {}, 발급시간: {}, 만료시간: {}, 현재시간: {}",
                    claims.getSubject(),
                    claims.getIssuedAt(),
                    expiration,
                    now);

            if (expiration != null && expiration.before(now)) {
                log.debug("토큰이 만료되었습니다");
                return false;
            }

            log.debug("토큰 검증 성공");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    // Claims 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }

    // 이메일 추출
    public String extractEmail(String token) {
        String subject = extractAllClaims(token).getSubject();
        return subject.replace(jwtProperties.getSubjectPrefix(), "");
    }

    // 만료 시간 계산
    private Date calculateExpirationDate(long expirationTime) {
        return new Date(new Date().getTime() + expirationTime);
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}