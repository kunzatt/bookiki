package com.corp.bookiki.auth.service;

import com.corp.bookiki.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService delegate; // @SpyBean 대신 @Mock 사용

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_SUB = "12345";

    @BeforeEach
    void setUp() {
        // delegate를 CustomOAuth2UserService에 주입
        ReflectionTestUtils.setField(customOAuth2UserService, "delegate", delegate);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        log.info("테스트 환경 설정 완료");
    }

    @Test
    @DisplayName("이메일 없는 경우 예외 발생")
    void loadUser_NoEmail_ThrowsException() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", TEST_SUB);  // 이메일 제외

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);
        log.info("이메일 없는 OAuth2User 설정 완료");

        // when & then
        assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(userRequest));
        log.info("이메일 없는 경우 예외 발생 확인");
    }
}