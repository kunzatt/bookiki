package com.corp.bookiki.jwt.handler;

import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
class LoginSuccessHandlerTest {

    @InjectMocks
    private LoginSuccessHandler loginSuccessHandler;

    @Mock
    private JWTService jwtService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Authentication authentication;
    private SecurityUserAdapter securityUserAdapter;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_ACCESS_TOKEN = "test.access.token";
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 객체 생성
        UserEntity testUser = UserEntity.builder()
                .email(TEST_EMAIL)
                .role(Role.USER)
                .build();
        securityUserAdapter = new SecurityUserAdapter(testUser);

        // Authentication 객체 생성
        authentication = new UsernamePasswordAuthenticationToken(
                securityUserAdapter,
                null,
                securityUserAdapter.getAuthorities()
        );

        // frontendUrl 설정
        ReflectionTestUtils.setField(loginSuccessHandler, "frontendUrl", "http://localhost:3000");

        log.info("테스트 환경 설정 완료");
    }

    @Nested
    @DisplayName("로그인 성공 처리 테스트")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("로그인 성공 시 토큰 발급 및 쿠키 설정 성공")
        void onAuthenticationSuccess_Success() throws IOException {
            // given
            given(jwtService.generateAccessToken(any(SecurityUserAdapter.class)))
                    .willReturn(TEST_ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(any(SecurityUserAdapter.class)))
                    .willReturn(TEST_REFRESH_TOKEN);
            log.info("JWT 서비스 Mock 설정 완료");

            // when
            loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            log.info("로그인 성공 핸들러 실행 완료");

            // then
            verify(jwtService).generateAccessToken(any(SecurityUserAdapter.class));
            verify(jwtService).generateRefreshToken(any(SecurityUserAdapter.class));
            verify(cookieUtil).addCookie(eq(response), eq("access_token"), eq(TEST_ACCESS_TOKEN));
            verify(cookieUtil).addCookie(eq(response), eq("refresh_token"), eq(TEST_REFRESH_TOKEN));
            verify(response).setStatus(HttpServletResponse.SC_OK);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            log.info("토큰 생성 및 쿠키 설정 검증 완료");
        }

        @Test
        @DisplayName("토큰 생성 실패 시 500 에러 응답")
        void onAuthenticationSuccess_WhenTokenGenerationFails_Returns500() throws IOException {
            // given
            given(jwtService.generateAccessToken(any(SecurityUserAdapter.class)))
                    .willThrow(new RuntimeException("토큰 생성 실패"));
            log.info("토큰 생성 실패 상황 Mock 설정 완료");

            // when
            loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            log.info("로그인 성공 핸들러 실행 완료");

            // then
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.info("에러 상태 코드 설정 검증 완료");
        }
    }
}