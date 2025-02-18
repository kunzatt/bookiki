package com.corp.bookiki.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.corp.bookiki.global.annotation.CurrentUser;
import com.corp.bookiki.global.error.dto.ErrorResponse;
import com.corp.bookiki.user.dto.AuthUser;
import com.corp.bookiki.user.dto.PasswordResetEmailRequest;
import com.corp.bookiki.user.dto.PasswordResetRequest;
import com.corp.bookiki.user.dto.PasswordUpdateRequest;
import com.corp.bookiki.user.service.PasswordService;

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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password")
@Tag(name = "비밀번호 API", description = "비밀번호 재설정 및 변경 관련 API")
public class PasswordUpdateController {

	private final PasswordService passwordService;

	@Operation(
		summary = "비밀번호 재설정 이메일 발송",
		description = "사용자 이름과 이메일이 일치하는 경우에만 비밀번호 재설정 링크가 포함된 이메일을 발송합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "이메일 발송 성공",
			content = @Content(mediaType = "application/json")
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 입력값",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "이메일 형식 오류",
						value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "잘못된 이메일 형식입니다",
                            "errors": []
                        }
                        """
					),
					@ExampleObject(
						name = "사용자 정보 불일치",
						value = """
                        {
                            "timestamp": "2024-01-23T10:00:00",
                            "status": 400,
                            "message": "사용자 이름과 이메일이 일치하지 않습니다",
                            "errors": []
                        }
                        """
					)
				}
			)
		)
	})
	@PostMapping("/reset-email")
	public ResponseEntity<Void> sendPasswordResetEmail(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "이메일과 사용자 이름 정보",
			required = true,
			content = @Content(
				schema = @Schema(implementation = PasswordResetEmailRequest.class),
				examples = @ExampleObject(
					value = """
                    {
                        "email": "user@example.com",
                        "userName": "user@example.com"
                    }
                    """
				)
			)
		)
		@Valid @RequestBody PasswordResetEmailRequest request
	) {
		passwordService.sendPasswordResetEmail(request.getEmail(), request.getUserName());
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "비밀번호 재설정",
		description = "이메일로 받은 토큰을 사용하여 비밀번호를 재설정합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "비밀번호 재설정 성공"
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 입력값 또는 토큰",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                    {
                        "timestamp": "2024-01-23T10:00:00",
                        "status": 400,
                        "message": "유효하지 않은 토큰입니다",
                        "errors": []
                    }
                    """
				)
			)
		)
	})
	@PutMapping("/reset/{token}")
	public ResponseEntity<Void> resetPassword(
		@Parameter(description = "비밀번호 재설정 토큰", required = true)
		@PathVariable String token,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "새로운 비밀번호 정보",
			required = true,
			content = @Content(
				schema = @Schema(implementation = PasswordResetRequest.class),
				examples = @ExampleObject(
					value = """
                    {
                        "newPassword": "새비밀번호123!",
                        "newPasswordConfirm": "새비밀번호123!"
                    }
                    """
				)
			)
		)
		@Valid @RequestBody PasswordResetRequest request
	) {
		passwordService.resetPassword(token, request.getNewPassword(), request.getNewPasswordConfirm());
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "비밀번호 변경",
		description = "로그인된 사용자의 비밀번호를 변경합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "비밀번호 변경 성공"
		),
		@ApiResponse(
			responseCode = "400",
			description = "현재 비밀번호 불일치 또는 새 비밀번호 확인 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                    {
                        "timestamp": "2024-01-23T10:00:00",
                        "status": 400,
                        "message": "현재 비밀번호가 일치하지 않습니다",
                        "errors": []
                    }
                    """
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증되지 않은 사용자",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					value = """
                    {
                        "timestamp": "2024-01-23T10:00:00",
                        "status": 401,
                        "message": "인증되지 않은 사용자입니다",
                        "errors": []
                    }
                    """
				)
			)
		)
	})
	@PutMapping("/update")
	public ResponseEntity<Void> updatePassword(
		@Parameter(hidden = true) @CurrentUser AuthUser authUser,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "비밀번호 변경 정보",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = PasswordUpdateRequest.class),
				examples = @ExampleObject(
					value = """
                    {
                        "currentPassword": "현재비밀번호123!",
                        "newPassword": "새비밀번호123!",
                        "newPasswordConfirm": "새비밀번호123!"
                    }
                    """
				)
			)
		)
		@Valid @RequestBody PasswordUpdateRequest request
	) {
		passwordService.updatePassword(
			authUser.getId(),
			request.getCurrentPassword(),
			request.getNewPassword(),
			request.getNewPasswordConfirm()
		);
		return ResponseEntity.ok().build();
	}
}