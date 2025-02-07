package com.corp.bookiki.user.service;

import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.GoogleOAuth2Request;
import com.corp.bookiki.user.dto.NaverOAuth2Request;
import com.corp.bookiki.user.dto.OAuth2Request;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // OAuth2 서비스 구분 (구글, 네이버)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Request oauth2Request = getOAuth2Request(registrationId, oAuth2User);

        String email = oauth2Request.getEmail();
        String name = oauth2Request.getName();
        Provider provider = getProvider(registrationId);

        UserEntity user = userRepository.findByEmailAndProvider(email, provider)
                .orElse(null);

        if (user == null) {
            // 임시 토큰 생성
            String temporaryToken = jwtService.generateTemporaryToken(email, provider);
            throw new OAuth2AuthenticationException("Temporary token: " + temporaryToken);
        }

        return new CustomUserDetails(user);
    }

    private OAuth2Request getOAuth2Request(String registrationId, OAuth2User oAuth2User) {
        if ("google".equals(registrationId)) {
            return new GoogleOAuth2Request(oAuth2User.getAttributes());
        } else if ("naver".equals(registrationId)) {
            return new NaverOAuth2Request(oAuth2User.getAttributes());
        }
        throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 서비스입니다");
    }

    private Provider getProvider(String registrationId) {
        if ("google".equals(registrationId)) {
            return Provider.GOOGLE;
        } else if ("naver".equals(registrationId)) {
            return Provider.NAVER;
        }
        throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 서비스입니다");
    }
}
