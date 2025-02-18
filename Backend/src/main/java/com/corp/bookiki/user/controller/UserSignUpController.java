package com.corp.bookiki.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.service.UserSignUpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/user/signup")
@RequiredArgsConstructor
@Tag(name = "회원가입 API", description = "회원가입 관련 API")
@Slf4j
public class UserSignUpController {

	private final UserSignUpService userSignUpService;

	@Operation(summary = "이메일 회원가입", description = "이메일을 통한 회원가입을 처리합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "회원가입 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(type = "string"),
				examples = @ExampleObject(value = "\"회원가입이 완료되었습니다.\"")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 입력값 또는 중복된 값",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "유효성 검증 실패",
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
					),
					@ExampleObject(
						name = "이메일 중복",
						value = """
							{
							    "timestamp": "2024-01-23T10:00:00",
							    "status": 400,
							    "message": "이미 존재하는 이메일입니다",
							    "errors": []
							}
							"""
					)
				}
			)
		)
	})
	@PostMapping
	public ResponseEntity<String> registerWithEmail(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "회원가입 정보",
			required = true,
			content = @Content(schema = @Schema(implementation = UserSignUpRequest.class))
		)
		@Valid @RequestBody UserSignUpRequest request
	) {
		log.info("이메일로 회원가입: {}", request.getEmail());
		userSignUpService.signUp(request);
		return ResponseEntity.ok("회원가입이 완료되었습니다.");
	}

	@Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일 중복 여부를 확인합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "이메일 사용 가능",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(type = "string"),
				examples = @ExampleObject(value = "\"이메일이 중복 되지 않습니다.\"")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 존재하는 이메일입니다",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping("/email/check")
	public ResponseEntity<String> checkEmailDuplicate(
		@Parameter(description = "확인할 이메일", required = true, example = "user@example.com")
		@RequestParam String email
	) {
		userSignUpService.checkEmailDuplicate(email);
		log.info("이메일 중복 확인: {}", email);
		return ResponseEntity.ok("이메일이 중복 되지 않습니다.");
	}

	@Operation(summary = "사번 중복 확인", description = "회원가입 시 사번 중복 여부를 확인합니다.")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "사번 사용 가능",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(type = "string"),
				examples = @ExampleObject(value = "\"사번이 중복 되지 않습니다.\"")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 존재하는 사번입니다",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@GetMapping("/company-id/check")
	public ResponseEntity<String> checkCompanyId(
		@Parameter(description = "확인할 사번", required = true, example = "확인할 사번")
		@RequestParam String companyId
	) {
		log.info("사번 중복 확인: {}", companyId);
		userSignUpService.checkEmployeeIdDuplicate(companyId);
		return ResponseEntity.ok("사번이 중복 되지 않습니다.");
	}
}