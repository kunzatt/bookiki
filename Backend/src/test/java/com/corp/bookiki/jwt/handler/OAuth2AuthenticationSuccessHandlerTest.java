package com.corp.bookiki.jwt.handler;

import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.util.CookieUtil;
import jakarta.servlet.ServletException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class OAuth2AuthenticationSuccessHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Mock
    private JWTService jwtService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_ACCESS_TOKEN = "test.access.token";
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private static final String TEST_TEMPORARY_TOKEN = "test.temporary.token";
    private static final String FRONTEND_URL = "http://localhost:3000";

    private Authentication authentication;
    private SecurityUserAdapter securityUserAdapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oauth2AuthenticationSuccessHandler, "frontendUrl", FRONTEND_URL);
    }

    @Nested
    @DisplayName("신규 사용자 처리")
    class NewUserTests {
        @BeforeEach
        void setUp() {
            UserEntity testUser = UserEntity.builder()
                    .email(TEST_EMAIL)
                    .role(Role.USER)
                    .build();
            securityUserAdapter = new SecurityUserAdapter(testUser);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("isNewUser", true);
            ReflectionTestUtils.setField(securityUserAdapter, "attributes", attributes);

            authentication = new UsernamePasswordAuthenticationToken(
                    securityUserAdapter,
                    null,
                    securityUserAdapter.getAuthorities()
            );
        }

        @Test
        @DisplayName("임시 토큰 발급 및 추가 정보 페이지로 리다이렉트")
        void shouldRedirectToAdditionalInfoPage() throws IOException, ServletException {
            // given
            when(jwtService.generateTemporaryToken(TEST_EMAIL))
                    .thenReturn(TEST_TEMPORARY_TOKEN);
            when(jwtService.getTemporaryTokenExpiration())
                    .thenReturn(300000L);

            // when
            oauth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(cookieUtil).addCookie(eq(response), eq("temporary_token"), eq(TEST_TEMPORARY_TOKEN), eq(300));
            verify(response).sendRedirect(FRONTEND_URL + "/oauth/google/additional");
        }
    }

    @Nested
    @DisplayName("기존 사용자 처리")
    class ExistingUserTests {
        @BeforeEach
        void setUp() {
            UserEntity testUser = UserEntity.builder()
                    .email(TEST_EMAIL)
                    .role(Role.USER)
                    .build();
            securityUserAdapter = new SecurityUserAdapter(testUser);
            authentication = new UsernamePasswordAuthenticationToken(
                    securityUserAdapter,
                    null,
                    securityUserAdapter.getAuthorities()
            );
        }

        @Test
        @DisplayName("토큰 발급 및 로그인 콜백 페이지로 리다이렉트")
        void shouldRedirectToLoginCallback() throws IOException, ServletException {
            // given
            when(jwtService.generateAccessToken(any(SecurityUserAdapter.class)))
                    .thenReturn(TEST_ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(any(SecurityUserAdapter.class)))
                    .thenReturn(TEST_REFRESH_TOKEN);

            // when
            oauth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(cookieUtil).addCookie(eq(response), eq("access_token"), eq(TEST_ACCESS_TOKEN));
            verify(cookieUtil).addCookie(eq(response), eq("refresh_token"), eq(TEST_REFRESH_TOKEN));
            verify(response).sendRedirect(FRONTEND_URL + "/login/callback");
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ErrorTests {
        @BeforeEach
        void setUp() {
            UserEntity testUser = UserEntity.builder()
                    .email(TEST_EMAIL)
                    .role(Role.USER)
                    .build();
            securityUserAdapter = new SecurityUserAdapter(testUser);
            authentication = new UsernamePasswordAuthenticationToken(
                    securityUserAdapter,
                    null,
                    securityUserAdapter.getAuthorities()
            );
        }

        @Test
        @DisplayName("처리 중 예외 발생시 에러 페이지로 리다이렉트")
        void shouldRedirectToErrorPage() throws IOException, ServletException {
            // given
            doThrow(new RuntimeException("토큰 생성 실패"))
                    .when(jwtService).generateAccessToken(any());

            // when
            oauth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

            // then
            verify(response).sendRedirect(FRONTEND_URL + "/error");
        }
    }
}