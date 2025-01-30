package com.corp.bookiki.jwt.handler;


import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler  implements LogoutHandler {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final JWTService jwtService;
    private final CookieUtil cookieUtil;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        log.debug("로그아웃 처리 시작");

        try {
            String refreshToken = cookieUtil.extractTokenFromCookies(request, REFRESH_TOKEN);

            if (refreshToken != null) {
                String userEmail = jwtService.extractUserEmail(refreshToken);
                log.info("사용자 로그아웃 처리 중: {}", userEmail);
                jwtService.invalidateRefreshToken(userEmail);
            }

            cookieUtil.removeCookie(response, ACCESS_TOKEN);
            cookieUtil.removeCookie(response, REFRESH_TOKEN);

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_OK);

            log.info("로그아웃 처리 완료");
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
