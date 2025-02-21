package com.corp.bookiki.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.corp.bookiki.user.dto.SendEmailRequest;
import com.corp.bookiki.user.dto.VerifyCodeRequest;
import com.corp.bookiki.user.service.EmailVerificationService;
import com.corp.bookiki.global.error.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth/email/verify")
@RequiredArgsConstructor
@Tag(name = "이메일 인증 API", description = "이메일 인증 코드 발송 및 확인 API")
@Slf4j
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;

	@Operation(
		summary = "인증 코드 발송",
		description = "입력된 이메일로 인증 코드를 발송합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "인증 코드 발송 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(type = "string"),
				examples = @ExampleObject(value = "")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 이메일 형식",
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
                                    "value": "invalid-email",
                                    "reason": "올바른 이메일 형식이 아닙니다"
                                }
                            ]
                        }
                        """
				)
			)
		)
	})
	@PostMapping
	public ResponseEntity<Void> sendVerificationCode(
		@RequestBody @Valid SendEmailRequest request
	) {
		log.info("인증 코드 발송 요청: {}", request.getEmail());
		emailVerificationService.sendVerificationCode(request.getEmail());
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "인증 코드 확인",
		description = "발송된 인증 코드의 유효성을 검증합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "인증 코드 확인 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(type = "string"),
				examples = @ExampleObject(value = "")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 인증 코드",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "잘못된 인증 코드입니다",
                            "errors": []
                        }
                        """
				)
			)
		)
	})
	@PostMapping("{email}")
	public ResponseEntity<Void> verifyCode(
		@PathVariable String email,
		@RequestBody @Valid VerifyCodeRequest request
	) {
		log.info("인증 코드 확인 요청: {}", email);
		emailVerificationService.verifyCode(email, request.getCode());
		return ResponseEntity.ok().build();
	}
}