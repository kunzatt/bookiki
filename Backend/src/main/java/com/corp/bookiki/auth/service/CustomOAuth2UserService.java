package com.corp.bookiki.auth.service;

import com.corp.bookiki.user.entity.Role;
import com.corp.bookiki.user.entity.SecurityUser;
import com.corp.bookiki.user.repository.SecurityUserRepository;
import com.corp.bookiki.user.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserDetailService userDetailService;
    private final SecurityUserRepository securityUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        try {
            SecurityUser existingUser = userDetailService.loadUserByUsername(email);
            return SecurityUser.builder()
                    .id(existingUser.getId())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .userName(existingUser.getUserName())
                    .role(existingUser.getRole())
                    .attributes(attributes)
                    .build();
        } catch (UsernameNotFoundException e) {
            log.debug("신규 사용자 감지: {}", email);
            SecurityUser tempUser = SecurityUser.builder()
                    .email(email)
                    .role(Role.USER)
                    .attributes(attributes)
                    .build();

            return securityUserRepository.save(tempUser);
        }
    }
}
