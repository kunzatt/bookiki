package com.corp.bookiki.util;

import com.corp.bookiki.user.dto.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JWTUtil {

    private final SecretKey key;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;

    public JWTUtil(@Value("${spring.jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateToken(AuthUser user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();
    }

    // JWT 토큰 파싱해 클레임(내부 데이터) 추출
    public Claims extractClaims(String token) {
        return Jwts.parser()    //JWT 파서 생성
                .verifyWith(key)    //서명 검증에 사용할 키 설정
                .build()
                .parseSignedClaims(token)   //토큰 파싱 및 서명 검증
                .getPayload();  //검증된 클레임 반환
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }


}
