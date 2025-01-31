package com.corp.bookiki.jwt;

import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UserEntity testUser;
    private SecurityUserAdapter securityUserAdapter;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_TOKEN = "test.token.value";

    @BeforeEach
    void setUp() {
        // 테스트용 만료 시간 설정
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "temporaryTokenExpiration", 300000L);

        // 테스트용 사용자 객체 생성
        testUser = UserEntity.builder()
                .email(TEST_EMAIL)
                .role(Role.USER)
                .build();
        securityUserAdapter = new SecurityUserAdapter(testUser);

        log.info("테스트 환경 설정 완료");
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class GenerateToken {

        @Test
        @DisplayName("SecurityUserAdapter로 액세스 토큰 생성 성공")
        void generateAccessToken_WithSecurityUserAdapter_Success() {
            // given
            given(tokenProvider.createAccessToken(any())).willReturn(TEST_TOKEN);
            log.info("토큰 생성 Mock 설정 완료");

            // when
            String token = jwtService.generateAccessToken(securityUserAdapter);
            log.info("액세스 토큰 생성됨: {}", token);

            // then
            assertNotNull(token);
            assertEquals(TEST_TOKEN, token);
            log.info("토큰 생성 검증 완료");
        }

        @Test
        @DisplayName("UserEntity로 리프레시 토큰 생성 성공")
        void generateRefreshToken_WithUserEntity_Success() {
            // given
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            given(tokenProvider.generateToken(any(), anyLong())).willReturn(TEST_TOKEN);
            log.info("리프레시 토큰 생성 Mock 설정 완료");

            // when
            String token = jwtService.generateRefreshToken(testUser);
            log.info("리프레시 토큰 생성됨: {}", token);

            // then
            assertNotNull(token);
            assertEquals(TEST_TOKEN, token);
            verify(valueOperations).set(
                    eq("RT:" + TEST_EMAIL),
                    eq(TEST_TOKEN),
                    eq(86400000L),
                    eq(TimeUnit.MILLISECONDS)
            );
            log.info("토큰 생성 및 Redis 저장 검증 완료");
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void isValid_WithValidToken_Success() {
            // given
            Claims mockClaims = mock(Claims.class);
            given(mockClaims.getSubject()).willReturn(TEST_EMAIL);
            given(mockClaims.getExpiration()).willReturn(new java.util.Date(System.currentTimeMillis() + 1000000));
            given(tokenProvider.parseClaims(TEST_TOKEN)).willReturn(mockClaims);
            given(tokenProvider.validateToken(TEST_TOKEN)).willReturn(true);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(TEST_EMAIL)
                    .password("")
                    .authorities("ROLE_USER")
                    .build();
            log.info("토큰 검증 Mock 설정 완료");

            // when & then
            assertTrue(jwtService.isValid(TEST_TOKEN, userDetails));
            log.info("토큰 검증 성공");
        }

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        void isValid_WithExpiredToken_ThrowsException() {
            // given
            Claims mockClaims = mock(Claims.class);
            given(mockClaims.getSubject()).willReturn(TEST_EMAIL);
            given(mockClaims.getExpiration()).willReturn(new java.util.Date(0));
            given(tokenProvider.parseClaims(TEST_TOKEN)).willReturn(mockClaims);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(TEST_EMAIL)
                    .password("")
                    .authorities("ROLE_USER")
                    .build();
            log.info("만료된 토큰 Mock 설정 완료");

            // when & then
            assertThrows(BusinessException.class, () -> jwtService.isValid(TEST_TOKEN, userDetails));
            log.info("만료된 토큰 검증 실패 확인");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RotateTokens {

        @Test
        @DisplayName("토큰 갱신 성공")
        void rotateTokens_Success() {
            // given
            String oldRefreshToken = "old.refresh.token";
            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";

            // Redis mock 설정
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            given(valueOperations.get("RT:" + TEST_EMAIL)).willReturn(oldRefreshToken);

            // Claims mock 설정
            Claims mockClaims = mock(Claims.class);
            given(mockClaims.getSubject()).willReturn(TEST_EMAIL);
            given(mockClaims.getExpiration()).willReturn(new Date(System.currentTimeMillis() + 1000000));

            // TokenProvider mock 설정
            given(tokenProvider.parseClaims(oldRefreshToken)).willReturn(mockClaims);
            given(tokenProvider.validateToken(oldRefreshToken)).willReturn(true);
            given(tokenProvider.createAccessToken(any())).willReturn(newAccessToken);
            given(tokenProvider.generateToken(any(), anyLong())).willReturn(newRefreshToken);

            log.info("토큰 갱신 Mock 설정 완료");

            // when
            Map<String, String> tokens = jwtService.rotateTokens(oldRefreshToken, securityUserAdapter);
            log.info("토큰 갱신 완료: accessToken={}, refreshToken={}",
                    tokens.get("accessToken"), tokens.get("refreshToken"));

            // then
            assertAll(
                    () -> assertEquals(newAccessToken, tokens.get("accessToken")),
                    () -> assertEquals(newRefreshToken, tokens.get("refreshToken"))
            );

            // 검증
            verify(redisTemplate).delete("RT:" + TEST_EMAIL);
            verify(valueOperations).set(
                    eq("RT:" + TEST_EMAIL),
                    eq(newRefreshToken),
                    eq(86400000L),
                    eq(TimeUnit.MILLISECONDS)
            );
            log.info("갱신된 토큰 검증 완료");
        }
    }
}