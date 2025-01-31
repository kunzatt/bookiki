package com.corp.bookiki.auth.service;

import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @SpyBean
    private DefaultOAuth2UserService delegate;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_SUB = "12345";

    @BeforeEach
    void setUp() {
        delegate = spy(new DefaultOAuth2UserService());
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        log.info("테스트 환경 설정 완료");
    }

    @Nested
    @DisplayName("OAuth2 사용자 로드 테스트")
    class LoadUser {

        @Test
        @DisplayName("기존 사용자 로드 성공")
        void loadUser_ExistingUser_Success() throws Exception {
            // given
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", TEST_SUB);
            attributes.put("email", TEST_EMAIL);

            DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "sub"
            );

            UserEntity existingUser = UserEntity.builder()
                    .email(TEST_EMAIL)
                    .provider(Provider.GOOGLE)
                    .role(Role.USER)
                    .build();
            SecurityUserAdapter existingUserAdapter = new SecurityUserAdapter(existingUser);

            when(delegate.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);
            when(userDetailsService.loadUserByUsername(TEST_EMAIL))
                    .thenReturn(existingUserAdapter);
            log.info("기존 사용자 Mock 설정 완료");

            // when
            OAuth2User result = customOAuth2UserService.loadUser(userRequest);
            log.info("기존 사용자 처리 완료");

            // then
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(TEST_EMAIL, ((SecurityUserAdapter) result).getEmail()),
                    () -> assertFalse((Boolean) result.getAttributes().getOrDefault("isNewUser", false))
            );
            log.info("기존 사용자 검증 완료");
        }

        @Test
        @DisplayName("신규 사용자 생성 성공")
        void loadUser_NewUser_Success() throws Exception {
            // given
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", TEST_SUB);
            attributes.put("email", TEST_EMAIL);

            DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "sub"
            );

            UserEntity newUser = UserEntity.builder()
                    .email(TEST_EMAIL)
                    .provider(Provider.GOOGLE)
                    .role(Role.USER)
                    .build();

            when(delegate.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);
            when(userDetailsService.loadUserByUsername(TEST_EMAIL))
                    .thenThrow(new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
            when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);
            log.info("신규 사용자 Mock 설정 완료");

            // when
            OAuth2User result = customOAuth2UserService.loadUser(userRequest);
            log.info("신규 사용자 처리 완료");

            // then
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(TEST_EMAIL, ((SecurityUserAdapter) result).getEmail()),
                    () -> assertTrue((Boolean) result.getAttributes().get("isNewUser"))
            );
            verify(userRepository).save(any(UserEntity.class));
            log.info("신규 사용자 검증 완료");
        }

        @Test
        @DisplayName("이메일 없는 경우 예외 발생")
        void loadUser_NoEmail_ThrowsException() throws Exception {
            // given
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", TEST_SUB);  // 이메일 제외

            DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "sub"
            );

            when(delegate.loadUser(any(OAuth2UserRequest.class))).thenReturn(oauth2User);
            log.info("이메일 없는 OAuth2User 설정 완료");

            // when & then
            assertThrows(OAuth2AuthenticationException.class,
                    () -> customOAuth2UserService.loadUser(userRequest));
            log.info("이메일 없는 경우 예외 발생 확인");
        }
    }
}