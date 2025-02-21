package com.corp.bookiki.jwt.controller;

import com.corp.bookiki.global.error.exception.JWTException;
import com.corp.bookiki.jwt.service.JwtService;
import com.corp.bookiki.user.service.AuthService;
import com.nimbusds.oauth2.sdk.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "토큰 재발급 API", description = "사용자 로그인 및 인증 관련 API")
public class TokenReissueController {
    private final AuthService authService;
    private final JwtService jwtService;

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            if (refreshToken == null) {
                log.error("Refresh Token이 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("refresh_token_missing", "Refresh Token이 없습니다."));
            }

            // refreshToken에서 이메일 추출
            String email = jwtService.extractEmail(refreshToken);
            authService.reissueTokens(email, refreshToken, response);
            return ResponseEntity.ok("토큰이 재발급되었습니다.");

        } catch (JWTException ex) {
            log.error("토큰 재발급 실패: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("invalid_token", ex.getMessage()));
        } catch (Exception ex) {
            log.error("토큰 재발급 실패: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("server_error", "토큰 재발급 중 오류가 발생했습니다."));
        }
    }
}

@Getter
@AllArgsConstructor
class ErrorResponse {
    private String error;
    private String message;
}
