package com.corp.bookiki.global.security.handler;

import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.security.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    //private final CookieUtil cookieUtil;
    @Value("${FRONTEND_URL}")
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
            JwtService jwtService,
            @Value("${FRONTEND_URL}") String frontendUrl) {
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @PostConstruct
    public void checkRedirectUri() {
        ClientRegistration registration = clientRegistrationRepository
                .findByRegistrationId("google");
        log.info("Redirect URI: {}", registration.getRedirectUri());
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();
        log.debug("OAuth2 인증 사용자: {}", user);

        if (user.getId() == null) {
            // 추가 정보가 필요한 경우 (최초 로그인)
            log.debug("신규 OAuth2 유저 가입");
            String temporaryToken = jwtService.generateTemporaryToken(
                    userDetails.getEmail(),
                    userDetails.getUser().getProvider()  // UserEntity에서 직접 Provider 가져오기
            );
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    frontendUrl + "/oauth2/signup?token=" + temporaryToken
            );
        } else {
            // 기존 회원인 경우
            String accessToken = jwtService.generateAccessToken(
                    userDetails.getEmail(),
                    userDetails.getAuthorities()  // CustomUserDetails에서 권한 정보 가져오기
            );
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    frontendUrl + "/oauth2/callback?token=" + accessToken
            );
        }
    }
}
