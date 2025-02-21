package com.corp.bookiki.jwt.repository;

import com.corp.bookiki.jwt.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private static final String KEY_PREFIX = "RT:";

    /**
     * Refresh Token을 Redis에 저장
     * @param email 사용자 이메일
     * @param refreshToken Refresh Token 값
     * key = "RT:" + 이메일
     * 만료 시간은 JwtProperties에서 가져온 값
     */
    public void save(String email, String refreshToken) {
        String key = getKey(email);
        long expiration = jwtProperties.getRefreshTokenExpiration() / 1000; // milliseconds to seconds

        redisTemplate.opsForValue()
                .set(key, refreshToken, expiration, TimeUnit.SECONDS);
        log.debug("Refresh Token이 Redis에 저장되었습니다. Email: {}", email);
    }

    /**
     * 이메일로 저장된 Refresh Token 조회
     * @param email 사용자 이메일
     * @return 저장된 Refresh Token, 없으면 null
     */
    public String findByEmail(String email) {
        String key = getKey(email);
        String token = redisTemplate.opsForValue().get(key);
        log.debug("Refresh Token 조회 결과. Email: {}, Exists: {}", email, token != null);
        return token;
    }

    /**
     * 특정 Refresh Token 삭제
     * @param email 사용자 이메일
     */
    public void delete(String email) {
        String key = getKey(email);
        redisTemplate.delete(key);
        log.debug("Refresh Token이 Redis에서 삭제되었습니다. Email: {}", email);
    }

    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃 등에 사용)
     * @param email 사용자 이메일
     */
    public void deleteAllTokens(String email) {
        String key = getKey(email);
        redisTemplate.delete(key);
        log.debug("모든 Refresh Token이 Redis에서 삭제되었습니다. Email: {}", email);
    }

    /**
     * Redis에 저장될 key 생성
     * @param email 사용자 이메일
     * @return Redis key
     */
    private String getKey(String email) {
        return KEY_PREFIX + email;
    }
}
