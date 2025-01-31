package com.corp.bookiki.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    private final String secretKey = "c3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLWppd29vbi1zcHJpbmctYm9vdC1zZWN1cml0eS1qd3QtdHV0b3JpYWwK";
    private final long accessTokenExpiration = 3600000; // 1시간
    private final long refreshTokenExpiration = 86400000; // 24시간

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", refreshTokenExpiration);
        jwtTokenProvider.init(); // 초기화 메서드 호출
    }

    @Test
    @DisplayName("Access Token 생성 테스트")
    void createAccessToken() {
        // given
        String username = "testUser";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        given(authentication.getName()).willReturn(username);
        given(authentication.getAuthorities()).willReturn(List.of(authority));

        // when
        String token = jwtTokenProvider.createAccessToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT 형식 검증 (header.payload.signature)
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰에서 Authentication 객체 추출 테스트")
    void getAuthentication() {
        // given
        String username = "testUser";
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenReturn(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(username);
        assertThat(resultAuth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Claims 파싱 테스트")
    void parseClaims() {
        // given
        String username = "testUser";
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenReturn(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        Claims claims = jwtTokenProvider.parseClaims(token);

        // then
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("auth")).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void validateExpiredToken() {
        // given
        String username = "testUser";
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenReturn(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 토큰 유효 시간을 0으로 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 0);
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when & then
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("쿠키 생성 테스트")
    void createCookie() {
        // given
        String name = "refreshToken";
        String value = "test-token-value";
        long maxAge = 86400000; // 24시간

        // when
        Cookie cookie = jwtTokenProvider.createCookie(name, value, maxAge);

        // then
        assertThat(cookie.getName()).isEqualTo(name);
        assertThat(cookie.getValue()).isEqualTo(value);
        assertThat(cookie.getMaxAge()).isEqualTo(maxAge / 1000); // 초 단위 변환 확인
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }
}
