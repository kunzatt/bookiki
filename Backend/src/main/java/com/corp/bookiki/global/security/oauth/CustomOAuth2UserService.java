package com.corp.bookiki.global.security.oauth;

import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.dto.GoogleOAuth2Request;
import com.corp.bookiki.user.dto.NaverOAuth2Request;
import com.corp.bookiki.user.dto.OAuth2Request;
import com.corp.bookiki.user.entity.Provider;
import com.corp.bookiki.user.entity.Role;
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

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        log.debug("OAuth2 사용자 정보 로드 시작: {}", userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Request oauth2Request = getOAuth2Request(registrationId, oAuth2User);

        String email = oauth2Request.getEmail();
        String name = oauth2Request.getName();
        Provider provider = getProvider(registrationId);

        log.debug("OAuth2 사용자 조회 시도: email={}, provider={}", email, provider);

        Optional<UserEntity> user = userRepository.findByEmailAndProvider(email, provider);
        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }

        // 신규 회원인 경우 임시 UserEntity 생성
        UserEntity tempUser = UserEntity.builder()
                .email(email)
                .provider(provider)
                .role(Role.USER)
                .build();

        return new CustomUserDetails(tempUser);
    }
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        try {
//            log.debug("OAuth2 사용자 정보 로드 시작: {}", userRequest);
//
//            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
//            OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//            // OAuth2 서비스 구분 (구글, 네이버)
//            String registrationId = userRequest.getClientRegistration().getRegistrationId();
//            OAuth2Request oauth2Request = getOAuth2Request(registrationId, oAuth2User);
//
//            String email = oauth2Request.getEmail();
//            String name = oauth2Request.getName();
//            Provider provider = getProvider(registrationId);
//
//            log.debug("OAuth2 사용자 조회 시도: email={}, provider={}", email, provider);
//
//            Optional<UserEntity> userOptional = userRepository.findByEmailAndProvider(email, provider);
//
//            UserEntity user = userRepository.findByEmailAndProvider(email, provider)
//                    .orElseGet(() -> {
//                        String temporaryToken = jwtService.generateTemporaryToken(email, provider);
//                        OAuth2Error oauth2Error = new OAuth2Error(
//                                "signup_required",
//                                temporaryToken,
//                                null
//                        );
//                        throw new OAuth2AuthenticationException(oauth2Error);
//                    });
//
//            return new CustomUserDetails(user);
//
//        } catch (OAuth2AuthenticationException e) {
//            // signup_required 예외는 그대로 전파
//            log.debug("OAuth2 신규 사용자 감지: {}", e.getMessage());
//            throw e;
//        } catch (Exception ex) {
//            // 기타 예외는 로깅 후 OAuth2AuthenticationException으로 변환
//            log.error("OAuth2 인증 처리 중 오류 발생", ex);
//            OAuth2Error oauth2Error = new OAuth2Error(
//                    "authentication_error",
//                    "인증 처리 중 오류가 발생했습니다.",
//                    null
//            );
//            throw new OAuth2AuthenticationException(oauth2Error, ex);
//        }
//    }

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
