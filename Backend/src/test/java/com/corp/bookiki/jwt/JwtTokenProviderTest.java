package com.corp.bookiki.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private Authentication authentication;

    String secretKey = Base64.getEncoder().encodeToString(
            Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded()
    );

    @BeforeEach
    void setUp() {
        // 테스트용 시크릿 키와 만료 시간 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 86400000L); // 24시간

        // 테스트용 Authentication 객체 생성
        authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 초기화 메서드 호출
        jwtTokenProvider.init();
        log.info("테스트 환경 설정 완료");
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class CreateToken {

        @Test
        @DisplayName("유효한 인증 정보로 액세스 토큰 생성 성공")
        void createAccessToken_WhenValidAuthentication_ThenSuccess() {
            // when
            String token = jwtTokenProvider.createAccessToken(authentication);
            log.info("액세스 토큰 생성됨: {}", token);

            // then
            assertNotNull(token);
            assertTrue(jwtTokenProvider.validateToken(token));
            log.info("토큰 생성 및 검증 성공");
        }

        @Test
        @DisplayName("토큰에서 올바른 Authentication 정보 추출")
        void getAuthentication_WhenValidToken_ThenSuccess() {
            // given
            String token = jwtTokenProvider.createAccessToken(authentication);
            log.info("테스트용 토큰 생성: {}", token);

            // when
            Authentication extractedAuth = jwtTokenProvider.getAuthentication(token);
            log.info("토큰에서 추출된 사용자명: {}", extractedAuth.getName());

            // then
            assertEquals(authentication.getName(), extractedAuth.getName());
            log.info("추출된 인증 정보 검증 성공");
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_WhenValidToken_ThenSuccess() {
            // given
            String token = jwtTokenProvider.createAccessToken(authentication);
            log.info("검증용 토큰 생성: {}", token);

            // when & then
            assertTrue(jwtTokenProvider.validateToken(token));
            log.info("토큰 유효성 검증 성공");
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 실패")
        void validateToken_WhenInvalidToken_ThenFail() {
            // given
            String invalidToken = "invalid.token.format";
            log.info("잘못된 형식의 토큰으로 테스트 시작");

            // when & then
            assertFalse(jwtTokenProvider.validateToken(invalidToken));
            log.info("잘못된 토큰 검증 실패 확인");
        }
    }

    @Nested
    @DisplayName("쿠키 생성 테스트")
    class CreateCookie {

        @Test
        @DisplayName("토큰으로 쿠키 생성 성공")
        void createCookie_WhenValidParameters_ThenSuccess() {
            // given
            String name = "accessToken";
            String value = "token-value";
            long maxAge = 3600000L;
            log.info("쿠키 생성 파라미터 설정: name={}, maxAge={}ms", name, maxAge);

            // when
            Cookie cookie = jwtTokenProvider.createCookie(name, value, maxAge);
            log.info("쿠키 생성됨: {}", cookie.getName());

            // then
            assertAll(
                    () -> assertEquals(name, cookie.getName()),
                    () -> assertEquals(value, cookie.getValue()),
                    () -> assertEquals(3600, cookie.getMaxAge()),
                    () -> assertTrue(cookie.isHttpOnly()),
                    () -> assertTrue(cookie.getSecure())
            );
            log.info("생성된 쿠키 속성 검증 완료");
        }
    }
}