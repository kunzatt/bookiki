package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.BusinessException;
import com.corp.bookiki.jwt.service.JWTService;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.OAuth2SignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserSignUpService;
import com.corp.bookiki.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2 회원가입 API", description = "소셜 로그인 회원가입 완료 처리 API")
public class OAuth2SignUpController {

    private final JWTService jwtService;
    private final UserSignUpService userSignUpService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Operation(
            summary = "OAuth2 회원가입 완료",
            description = """
            OAuth2 로그인 후 추가 정보(사번, 이름)를 입력받아 회원가입을 완료합니다.
            임시 토큰은 삭제되고, 정식 access token과 refresh token이 쿠키로 발급됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthUser.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "email": "user@example.com",
                                "userName": "홍길동",
                                "companyId": "EMP001",
                                "role": "USER",
                                "provider": "google"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "timestamp": "2024-01-23T10:00:00",
                                "status": 400,
                                "message": "잘못된 입력값입니다",
                                "errors": [
                                    {
                                        "field": "companyId",
                                        "value": "",
                                        "reason": "사번은 필수 입력값입니다"
                                    }
                                ]
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "timestamp": "2024-01-23T10:00:00",
                                "status": 401,
                                "message": "유효하지 않은 토큰입니다",
                                "errors": []
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "timestamp": "2024-01-23T10:00:00",
                                "status": 404,
                                "message": "사용자를 찾을 수 없습니다",
                                "errors": []
                            }
                            """
                            )
                    )
            )
    })
    @PostMapping("/{provider}/oauthsignup")
    public ResponseEntity<?> completeOAuthSignUp(
            @PathVariable String provider,
            @RequestBody @Valid OAuth2SignUpRequest request,
            @CookieValue(name = "temporary_token") String temporaryToken,
            HttpServletResponse response
    ) {
        // 임시 토큰에서 이메일 추출
        String email = jwtService.extractUserEmail(temporaryToken);

        // 사용자 정보 업데이트
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserEntity updatedUser = UserEntity.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .provider(user.getProvider())
                .companyId(request.getCompanyId())
                .userName(request.getUserName())
                .role(user.getRole())
                .build();
        UserEntity savedUser = userRepository.save(updatedUser);

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
