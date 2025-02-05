package com.corp.bookiki.user.controller;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.LoginRequest;
import com.corp.bookiki.user.dto.LoginResponse;
import com.corp.bookiki.user.service.AuthService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 로그인 및 인증 관련 API")
public class LoginController {

    private final AuthService authService;

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 쿠키로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "email": "test@example.com",
                                "userName": "홍길동",
                                "role": "USER"
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                            {
                                "timestamp": "2024-01-23T10:00:00",
                                "status": 401,
                                "message": "이메일 또는 비밀번호가 일치하지 않습니다",
                                "errors": []
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
                                        "field": "email",
                                        "value": "",
                                        "reason": "이메일은 필수 입력값입니다"
                                    }
                                ]
                            }
                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword(),
                    response
            );

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            log.error("인증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 이메일 또는 비밀번호 입니다.");

        }
    }
}