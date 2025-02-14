package com.corp.bookiki.user.controller;

import com.corp.bookiki.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 로그인 및 인증 관련 API")
public class LogoutController {

//    private final AuthService authService;
//
//    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 토큰을 무효화합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
//            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(HttpServletResponse response) {
//        log.debug("로그아웃 요청 시작");
//
//        // 루트 경로 쿠키 삭제
//        Cookie rootAccessToken = new Cookie("access_token", null);
//        rootAccessToken.setPath("/");
//        rootAccessToken.setMaxAge(0);
//        response.addCookie(rootAccessToken);
//        log.debug("access_token 쿠키 삭제됨 (path: /)");
//
//        Cookie rootRefreshToken = new Cookie("refresh_token", null);
//        rootRefreshToken.setPath("/");
//        rootRefreshToken.setMaxAge(0);
//        response.addCookie(rootRefreshToken);
//        log.debug("refresh_token 쿠키 삭제됨 (path: /)");
//
//        // /api/auth 경로 쿠키 삭제
//        Cookie authRefreshToken = new Cookie("refresh_token", null);
//        authRefreshToken.setPath("/api/auth");
//        authRefreshToken.setMaxAge(0);
//        response.addCookie(authRefreshToken);
//        log.debug("refresh_token 쿠키 삭제됨 (path: /api/auth)");
//
//        log.debug("로그아웃 처리 완료");
//        return ResponseEntity.ok().build();
//    }

}
