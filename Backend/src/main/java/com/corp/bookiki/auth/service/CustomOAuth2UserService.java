package com.corp.bookiki.auth.service;

import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            return processOAuth2User(registrationId, attributes);
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생: {}", e.getMessage());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("processing_error"),
                    "OAuth2 사용자 처리 중 오류가 발생했습니다."
            );
        }
    }

    private OAuth2User processOAuth2User(String registrationId, Map<String, Object> attributes) {
        String email = extractEmail(attributes);

        try {
            SecurityUserAdapter existingUser = (SecurityUserAdapter) userDetailsService.loadUserByUsername(email);
            existingUser.setAttributes(attributes);  // OAuth2 속성 업데이트
            return existingUser;
        } catch (UsernameNotFoundException e) {
            log.debug("신규 OAuth2 사용자 감지: {}", email);
            return createNewOAuth2User(email, attributes, registrationId);
        }
    }

    private String extractEmail(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email"),
                    "OAuth2 프로필에 이메일 정보가 없습니다."
            );
        }
        return email;
    }

    private SecurityUserAdapter createNewOAuth2User(String email, Map<String, Object> attributes, String registrationId) {
        UserEntity newUser = UserEntity.builder()
                .email(email)
                .provider(Provider.valueOf(registrationId.toUpperCase()))
                .build();

        UserEntity savedUser = userRepository.save(newUser);
        SecurityUserAdapter securityUser = new SecurityUserAdapter(savedUser);
        securityUser.setAttributes(attributes);  // OAuth2 속성 설정
        attributes.put("isNewUser", true);  // 신규 사용자 표시

        return securityUser;
    }
}
