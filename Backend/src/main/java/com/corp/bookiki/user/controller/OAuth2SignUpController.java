package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.adapter.SecurityUserAdapter;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.OAuth2SignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserSignUpService;
import com.corp.bookiki.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class OAuth2SignUpController {

    private final JWTService jwtService;
    private final UserSignUpService userSignUpService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @PostMapping("/{provider}/oauthsignup")
    public ResponseEntity<?> completeOAuthSignUp(
            @PathVariable String provider,
            @RequestBody @Valid OAuth2SignUpRequest request,
            @CookieValue(name = "temporary_token") String temporaryToken,
            HttpServletResponse response
    ) {
        // 임시 토큰에서 이메일 추출
        String email = jwtService.extractUserEmail(t
                // 사번 중복 체크
                userSignUpService.checkEmployeeIdDuplicate(request.getCompanyId());

        // 사용자 정보 업데이트
        SecurityUserAdapter user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user = UserEntity.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .companyId(request.getCompanyId())emporaryToken);

        .userName(request.getUserName())
        .build();
user = userRepository.save(user);

// 임시 토큰 삭제
        cookieUtil.removeCookie(response, "temporary_token");

// 정식 토큰 발급
String accessToken = jwtService.generateAccessToken(user);
String refreshToken = jwtService.generateRefreshToken(user);

// 쿠키에 토큰 설정
        cookieUtil.addCookie(response, "access_token", accessToken);
        cookieUtil.addCookie(response, "refresh_token", refreshToken);

        return ResponseEntity.ok(AuthUser.from(user));
        }
        }
