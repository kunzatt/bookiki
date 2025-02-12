package com.corp.bookiki.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "비밀번호 재설정 이메일 발송 요청")
public class PasswordResetEmailRequest {

	@Schema(description = "이메일", example = "user@example.com", required = true)
	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@Schema(description = "사용자 이름", example = "user@example.com", required = true)
	@NotBlank(message = "사용자 이름은 필수입니다")
	private String userName;
}